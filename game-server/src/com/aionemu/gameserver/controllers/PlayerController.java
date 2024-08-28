package com.aionemu.gameserver.controllers;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.handler.ShoutEventHandler;
import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.configs.main.*;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.controllers.observer.StanceObserver;
import com.aionemu.gameserver.custom.pvpmap.PvpMapService;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.animations.ActionAnimation;
import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.*;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.model.gameobjects.state.FlyState;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.stats.container.PlayerGameStats;
import com.aionemu.gameserver.model.summons.SummonMode;
import com.aionemu.gameserver.model.summons.UnsummonType;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.flypath.FlyPathEntry;
import com.aionemu.gameserver.model.templates.panels.SkillPanel;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.restrictions.PlayerRestrictions;
import com.aionemu.gameserver.services.*;
import com.aionemu.gameserver.services.conquerorAndProtectorSystem.ConquerorAndProtectorService;
import com.aionemu.gameserver.services.drop.DropService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.reward.StarterKitService;
import com.aionemu.gameserver.services.summons.SummonsService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.effect.EffectTemplate;
import com.aionemu.gameserver.skillengine.effect.RebirthEffect;
import com.aionemu.gameserver.skillengine.model.*;
import com.aionemu.gameserver.skillengine.model.Skill.SkillMethod;
import com.aionemu.gameserver.taskmanager.tasks.PlayerMoveTaskManager;
import com.aionemu.gameserver.taskmanager.tasks.TeamMoveUpdater;
import com.aionemu.gameserver.taskmanager.tasks.TeamStatUpdater;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.world.MapRegion;
import com.aionemu.gameserver.world.WorldType;
import com.aionemu.gameserver.world.geo.GeoService;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * This class is for controlling players.
 * 
 * @author -Nemesiss-, ATracer, xavier, Sarynth, RotO, xTz, KID, Sippolo
 */
public class PlayerController extends CreatureController<Player> {

	private static final Logger log = LoggerFactory.getLogger(PlayerController.class);
	private long lastAttackMillis = 0;
	private long lastAttackedMillis = 0;
	private StanceObserver stanceObserver;

	@Override
	public void see(VisibleObject object) {
		super.see(object);
		if (object instanceof Creature creature) {
			if (creature instanceof Npc npc) {
				PacketSendUtility.sendPacket(getOwner(), new SM_NPC_INFO(npc, getOwner()));
				if (npc instanceof Kisk) {
					if (getOwner().getRace() == ((Kisk) npc).getOwnerRace())
						PacketSendUtility.sendPacket(getOwner(), new SM_KISK_UPDATE((Kisk) npc));
				} else {
					QuestEngine.getInstance().onAtDistance(new QuestEnv(npc, getOwner(), 0));
				}
				DropService.getInstance().see(getOwner(), npc);
			} else if (creature instanceof Player player) {
				PacketSendUtility.sendPacket(getOwner(), new SM_PLAYER_INFO(player, getOwner().isAggroIconTo(player)));
				PacketSendUtility.sendPacket(getOwner(), new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
				if (player.isInPlayerMode(PlayerMode.RIDE))
					PacketSendUtility.sendPacket(getOwner(), new SM_EMOTION(player, EmotionType.RIDE, 0, player.ride.getNpcId()));
			} else if (creature instanceof Summon) {
				PacketSendUtility.sendPacket(getOwner(), new SM_NPC_INFO((Summon) creature, getOwner()));
			}
			if (!creature.getEffectController().isEmpty())
				PacketSendUtility.sendPacket(getOwner(), new SM_ABNORMAL_EFFECT(creature));
		} else if (object instanceof Gatherable || object instanceof StaticObject) {
			PacketSendUtility.sendPacket(getOwner(), new SM_GATHERABLE_INFO(object));
		} else if (object instanceof Pet pet) {
			PacketSendUtility.sendPacket(getOwner(), new SM_PET(pet));
			if (pet.getMaster().isInFlyingState())
				PacketSendUtility.sendPacket(getOwner(), new SM_PET_EMOTE(pet, PetEmote.FLY_START));
		} else if (object instanceof House) {
			PacketSendUtility.sendPacket(getOwner(), new SM_HOUSE_RENDER((House) object));
		} else if (object instanceof HouseObject) {
			PacketSendUtility.sendPacket(getOwner(), new SM_HOUSE_OBJECT((HouseObject<?>) object));
		}
	}

	@Override
	public void notSee(VisibleObject object, ObjectDeleteAnimation animation) {
		super.notSee(object, animation);
		if (object instanceof Pet) {
			PacketSendUtility.sendPacket(getOwner(), new SM_PET(object.getObjectId(), animation));
		} else if (object instanceof House) {
			PacketSendUtility.sendPacket(getOwner(), new SM_DELETE_HOUSE(((House) object).getAddress().getId()));
		} else if (object instanceof HouseObject) {
			PacketSendUtility.sendPacket(getOwner(), new SM_DELETE_HOUSE_OBJECT(object.getObjectId()));
		} else if (object instanceof Npc && ((Npc) object).isFlag()) {
			PacketSendUtility.sendPacket(getOwner(), new SM_DELETE(object, ObjectDeleteAnimation.DELAYED));
		} else {
			PacketSendUtility.sendPacket(getOwner(), new SM_DELETE(object, animation));
		}
	}

	@Override
	public void onHide() {
		super.onHide();
		DuelService.getInstance().fixTeamVisibility(getOwner());
	}

	@Override
	public void onHideEnd() {
		Pet pet = getOwner().getPet();
		if (pet != null && !PositionUtil.isInRange(getOwner(), pet, 3)) // client sends pet position only every 50m...
			pet.getPosition().setXYZH(getOwner().getX(), getOwner().getY(), getOwner().getZ(), getOwner().getHeading());
		super.onHideEnd();
	}

	public void updateNearbyQuests() {
		Map<Integer, Integer> nearbyQuestList = new HashMap<>();
		for (int questId : getOwner().getPosition().getMapRegion().getParent().getQuestIds()) {
			if (QuestService.checkStartConditions(getOwner(), questId, false, 2, false, false, false))
				nearbyQuestList.put(questId, QuestService.getLevelRequirementDiff(questId, getOwner().getCommonData().getLevel()));
		}
		PacketSendUtility.sendPacket(getOwner(), new SM_NEARBY_QUESTS(nearbyQuestList));
	}

	public void updateRepeatableQuests() {
		List<Integer> reapeatQuestList = new ArrayList<>();
		for (int questId : getOwner().getPosition().getMapRegion().getParent().getQuestIds()) {
			QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
			if (!template.isTimeBased())
				continue;
			if (QuestService.checkStartConditions(getOwner(), questId, false))
				reapeatQuestList.add(questId);
		}
		if (reapeatQuestList.size() > 0)
			PacketSendUtility.sendPacket(getOwner(), new SM_QUEST_REPEAT(reapeatQuestList));
	}

	@Override
	public void onEnterZone(ZoneInstance zone) {
		Player player = getOwner();
		if (!zone.canRide() && player.isInPlayerMode(PlayerMode.RIDE))
			player.unsetPlayerMode(PlayerMode.RIDE);
		ConquerorAndProtectorService.getInstance().onEnterZone(player, zone);
		InstanceService.onEnterZone(player, zone);
		ZoneName zoneName = zone.getAreaTemplate().getZoneName();
		if (zoneName == null)
			log.warn("No name found for a zone in map " + zone.getAreaTemplate().getWorldId() + " with xml name " + zone.getZoneTemplate().getXmlName());
		else
			QuestEngine.getInstance().onEnterZone(new QuestEnv(null, player, 0), zoneName);
	}

	@Override
	public void onLeaveZone(ZoneInstance zone) {
		Player player = getOwner();
		ConquerorAndProtectorService.getInstance().onLeaveZone(player, zone);
		InstanceService.onLeaveZone(player, zone);
		ZoneName zoneName = zone.getAreaTemplate().getZoneName();
		if (zoneName == null)
			log.warn("No name found for a zone in map " + zone.getAreaTemplate().getWorldId() + " with xml name " + zone.getZoneTemplate().getXmlName());
		else
			QuestEngine.getInstance().onLeaveZone(new QuestEnv(null, player, 0), zoneName);
	}

	/**
	 * Called when leaving a fly zone (like citadel of verteron) or a fly map (like the abyss).
	 */
	public void onLeaveFlyArea() {
		Player player = getOwner();
		if (!player.hasAccess(AdminConfig.FREE_FLIGHT)) {
			if (player.isInFlyingState()) {
				if (player.isInGlidingState()) {
					player.unsetFlyState(FlyState.FLYING);
					player.unsetState(CreatureState.FLYING);
					player.getLifeStats().triggerFpReduce();
					player.getGameStats().updateStatsAndSpeedVisually();
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.STOP_FLY), true);
				} else {
					player.getFlyController().endFly(true);
					if (player.isSpawned()) // not spawned means leaving by teleporter
						AuditLogger.log(player, "left fly zone in fly state at " + player.getPosition());
				}
			} else if (player.isInGlidingState()) {
				player.getLifeStats().triggerFpReduce();
			}
		}
	}

	public void onEnterFlyArea() {
		if (!getOwner().hasAccess(AdminConfig.FREE_FLIGHT)) {
			if (getOwner().isFlying()) {
				getOwner().getLifeStats().triggerFpReduce();
			}
		}
	}

	/**
	 * Should only be triggered from one place (life stats)
	 */
	// TODO [AT] move
	public void onEnterWorld() {
		if (getOwner().getPosition().getWorldMapInstance().getParent().isExceptBuff()) {
			if (!PvpMapService.getInstance().isOnPvPMap(getOwner()))
				getOwner().getEffectController().removeAllEffects();
		}

		for (Effect ef : getOwner().getEffectController().getAbnormalEffects()) {
			if (ef.isDeityAvatar()) {
				// remove abyss transformation if worldtype != abyss && worldtype != balaurea && worldType != panesterra
				if (getOwner().getWorldType() != WorldType.ABYSS && getOwner().getWorldType() != WorldType.BALAUREA
					&& getOwner().getWorldType() != WorldType.PANESTERRA || getOwner().isInInstance()) {
					ef.endEffect();
					getOwner().getEffectController().clearEffect(ef);
				}
			} else if (ef.getSkillTemplate().getDispelCategory() == DispelCategoryType.NPC_BUFF) {
				ef.endEffect();
				getOwner().getEffectController().clearEffect(ef);
			}
		}
	}

	@Override
	public void onDie(Creature lastAttacker, boolean sendDiePacket) {
		Player player = getOwner();
		player.getController().cancelCurrentSkill(null);
		setRebirthReviveInfo();
		Creature master = lastAttacker.getMaster();

		if (DuelService.getInstance().isDueling(player)) {
			boolean killedByOpponent = master instanceof Player && ((Player) master).isDueling(player);
			DuelService.getInstance().loseDuel(player);
			if (killedByOpponent) {
				player.getEffectController().removeByDispelSlotType(DispelSlotType.DEBUFF);
				if (player.getLifeStats().getHpPercentage() < 33) {
					player.getLifeStats().setCurrentHpPercent(33);
					player.getLifeStats().triggerRestoreOnRevive();
				}
				if (player.getLifeStats().getMpPercentage() < 33)
					player.getLifeStats().setCurrentMpPercent(33);
				if (master.getLifeStats().getHpPercentage() < 33)
					master.getLifeStats().setCurrentHpPercent(33);
				if (master.getLifeStats().getMpPercentage() < 33)
					master.getLifeStats().setCurrentMpPercent(33);
				return;
			}
		}

		// Release summon
		Summon summon = player.getSummon();
		if (summon != null)
			SummonsService.doMode(SummonMode.RELEASE, summon, UnsummonType.UNSPECIFIED);

		// setIsFlyingBeforeDead for PlayerReviveService
		if (player.isInState(CreatureState.FLYING))
			player.setIsFlyingBeforeDeath(true);

		// ride
		player.setPlayerMode(PlayerMode.RIDE, null);
		player.unsetState(CreatureState.RESTING);
		player.unsetState(CreatureState.FLOATING_CORPSE);

		// unset flying
		player.unsetState(CreatureState.FLYING);
		player.unsetState(CreatureState.GLIDING);
		player.unsetFlyState(FlyState.FLYING);
		player.unsetFlyState(FlyState.GLIDING);

		player.resetFearCount();
		player.resetSleepCount();
		player.resetParalyzeCount();

		// Effects removed with super.onDie()
		super.onDie(lastAttacker, sendDiePacket);

		if (player.isInInstance() && player.getPosition().getWorldMapInstance().getInstanceHandler().onDie(player, lastAttacker))
			return;

		MapRegion mapRegion = player.getPosition().getMapRegion();
		if (mapRegion != null && mapRegion.onDie(lastAttacker, player))
			return;

		doReward();

		if (master instanceof Npc || master.equals(player)) {
			if (player.getLevel() > 4 && !player.getEffectController().hasAbnormalEffect(Effect::isNoDeathPenalty))
				player.getCommonData().calculateExpLoss();
		}

		if (sendDiePacket && !player.getController().hasTask(TaskId.TELEPORT)) // don't show res options if the player is about to get teleported (see ResurrectBaseEffect)
			sendDieFromCreature(lastAttacker);

		QuestEngine.getInstance().onDie(new QuestEnv(null, player, 0));
	}

	private void setRebirthReviveInfo() {
		Player player = getOwner();
		// Store the effect info.
		List<Effect> effects = player.getEffectController().getAbnormalEffects();
		for (Effect effect : effects) {
			for (EffectTemplate template : effect.getEffectTemplates()) {
				if (template.getEffectId() == 160 && template instanceof RebirthEffect) {
					player.setRebirthEffect((RebirthEffect) template);
					return;
				}
			}
		}
		player.setRebirthEffect(null);
	}

	@Override
	public void onDespawn() {
		if (getOwner().isLooting())
			DropService.getInstance().closeDropList(getOwner(), getOwner().getLootingNpcOid());
		super.onDespawn();
	}

	public void sendDie() {
		sendDieFromCreature(getOwner());
	}

	private void sendDieFromCreature(Creature lastAttacker) {
		Player player = getOwner();
		if (player.getWorldId() == 400030000) {
			PacketSendUtility.sendPacket(player, new SM_DIE(player, 6));
			return;
		}
		int kiskTimeRemaining = (player.getKisk() != null ? player.getKisk().getRemainingLifetime() : 0);
		PacketSendUtility.sendPacket(player, new SM_DIE(player.canUseRebirthRevive(), player.haveSelfRezItem(), kiskTimeRemaining, 0, isInvader(player)));
	}

	private boolean isInvader(Player player) {
		if (player.getRace().equals(Race.ASMODIANS)) {
			return player.getWorldId() == 210060000;
		} else {
			return player.getWorldId() == 220050000;
		}
	}

	@Override
	public void doReward() {
		PvpService.getInstance().doReward(getOwner());
	}

	@Override
	public void onBeforeSpawn() {
		super.onBeforeSpawn();
		if (getOwner().isDead())
			return;
		if (getOwner().getIsFlyingBeforeDeath())
			getOwner().unsetState(CreatureState.FLOATING_CORPSE);
		else if (getOwner().isInState(CreatureState.DEAD))
			getOwner().unsetState(CreatureState.DEAD);
		getOwner().setState(CreatureState.ACTIVE);
	}

	@Override
	public void attackTarget(Creature target, int time, boolean skipChecks) {

		PlayerGameStats gameStats = getOwner().getGameStats();

		if (!PlayerRestrictions.canAttack(getOwner(), target))
			return;

		// client handles most distance checks beforehand, but for some cases we need to check it also
		if (!PositionUtil.isInAttackRange(getOwner(), target, getOwner().getGameStats().getAttackRange().getCurrent() / 1000f + 1)) {
			PacketSendUtility.sendPacket(getOwner(), SM_ATTACK_RESPONSE.TARGET_TOO_FAR_AWAY(gameStats.getAttackCounter()));
			return;
		}

		if (!GeoService.getInstance().canSee(getOwner(), target)) {
			PacketSendUtility.sendPacket(getOwner(), SM_ATTACK_RESPONSE.STOP_OBSTACLE_IN_THE_WAY(gameStats.getAttackCounter()));
			return;
		}

		if (target instanceof Npc) {
			QuestEngine.getInstance().onAttack(new QuestEnv(target, getOwner(), 0));
		}

		int attackSpeed = gameStats.getAttackSpeed().getCurrent();

		long milis = System.currentTimeMillis();
		// network ping..
		if (milis - lastAttackMillis + 300 < attackSpeed) {
			// hack
			PacketSendUtility.sendPacket(getOwner(), SM_ATTACK_RESPONSE.STOP_WITHOUT_MESSAGE(gameStats.getAttackCounter()));
			return;
		}
		lastAttackMillis = milis;

		super.attackTarget(target, time, true);
	}

	@Override
	public void onAttack(Creature attacker, Effect effect, TYPE type, int damage, boolean notifyAttack, LOG logId, AttackStatus attackStatus,
		HopType hopType) {
		if (getOwner().isDead())
			return;

		if (getOwner().isProtectionActive())
			return;

		// avoid killing players after duel
		if (!getOwner().equals(attacker) && attacker.getActingCreature() instanceof Player && !getOwner().isEnemy(attacker))
			return;

		cancelUseItem();
		cancelGathering();
		super.onAttack(attacker, effect, type, damage, notifyAttack, logId, attackStatus, hopType);

		if (attacker instanceof Npc) {
			ShoutEventHandler.onAttack((NpcAI) attacker.getAi(), getOwner());
			QuestEngine.getInstance().onAttack(new QuestEnv(attacker, getOwner(), 0));
		}

		lastAttackedMillis = System.currentTimeMillis();
	}

	public void useSkill(int skillId, int targetType, float x, float y, float z, int time) {
		Player player = getOwner();

		Skill skill = SkillEngine.getInstance().getSkillFor(player, skillId, player.getTarget());

		if (skill != null) {
			if (!PlayerRestrictions.canUseSkill(player, skill))
				return;

			skill.setTargetType(targetType, x, y, z);
			skill.setHitTime(time);
			skill.useSkill();
		}
	}

	public void useSkill(SkillTemplate template, int targetType, float x, float y, float z, int clientHitTime, int skillLevel) {
		Player player = getOwner();
		Skill skill = SkillEngine.getInstance().getSkillFor(player, template, player.getTarget());
		if (skill == null && player.isTransformed()) {
			SkillPanel panel = DataManager.PANEL_SKILL_DATA.getSkillPanel(player.getTransformModel().getPanelId());
			if (panel != null && panel.canUseSkill(template.getSkillId(), skillLevel)) {
				skill = SkillEngine.getInstance().getSkillFor(player, template, player.getTarget(), skillLevel);
			}
		}

		if (skill != null) {
			if (!PlayerRestrictions.canUseSkill(player, skill))
				return;

			skill.setTargetType(targetType, x, y, z);
			skill.setHitTime(clientHitTime);
			skill.useSkill();
		}
	}

	@Override
	public void onStartMove() {
		super.onStartMove();
		PlayerMoveTaskManager.getInstance().addPlayer(getOwner());
		cancelUseItem();
		cancelCurrentSkill(null);
	}

	@Override
	public void onMove() {
		super.onMove();
		if (getOwner().isInTeam())
			TeamMoveUpdater.getInstance().startTask(getOwner());
	}

	@Override
	public void onStopMove() {
		super.onStopMove();
		PlayerMoveTaskManager.getInstance().removePlayer(getOwner());
		cancelCurrentSkill(null);
		updateZone();
	}

	@Override
	public void cancelCurrentSkill(Creature lastAttacker) {
		cancelCurrentSkill(lastAttacker, SM_SYSTEM_MESSAGE.STR_SKILL_CANCELED());
	}

	@Override
	public void cancelCurrentSkill(Creature lastAttacker, SM_SYSTEM_MESSAGE message) {
		if (getOwner().getCastingSkill() == null) {
			return;
		}

		Player player = getOwner();
		Skill castingSkill = player.getCastingSkill();
		castingSkill.cancelCast();
		player.removeSkillCoolDown(castingSkill.getSkillTemplate().getCooldownId());
		player.setCasting(null);
		player.setNextSkillUse(0);
		if (castingSkill.getSkillMethod() == SkillMethod.CAST || castingSkill.getSkillMethod() == SkillMethod.CHARGE) {
			PacketSendUtility.broadcastPacket(player, new SM_SKILL_CANCEL(player, castingSkill.getSkillTemplate().getSkillId()), true);
			if (message != null)
				PacketSendUtility.sendPacket(player, message);
		} else if (castingSkill.getSkillMethod() == SkillMethod.ITEM) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED());
			player.removeItemCoolDown(castingSkill.getItemTemplate().getUseLimits().getDelayId());
			PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), castingSkill.getFirstTarget().getObjectId(),
				castingSkill.getItemObjectId(), castingSkill.getItemTemplate().getTemplateId(), 0, 3, 0), true);
		}

		if (lastAttacker instanceof Player && !lastAttacker.equals(getOwner())) {
			PacketSendUtility.sendPacket((Player) lastAttacker, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_SKILL_CANCELED());
		}
	}

	@Override
	public void cancelUseItem() {
		Player player = getOwner();
		Item usingItem = player.getUsingItem();
		player.setUsingItem(null);
		if (hasTask(TaskId.ITEM_USE)) {
			cancelTask(TaskId.ITEM_USE);
			PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), usingItem == null ? 0 : usingItem.getObjectId(),
				usingItem == null ? 0 : usingItem.getItemTemplate().getTemplateId(), 0, 3, 0), true);
		}
	}

	public void cancelGathering() {
		Player player = getOwner();
		if (player.getTarget() instanceof Gatherable g) {
			g.getController().finishGathering(player);
		}
	}

	@Override
	public void onDialogSelect(int dialogActionId, int prevDialogId, Player player, int questId, int extendedRewardIndex) {
		switch (dialogActionId) {
			case BUY:
				PacketSendUtility.sendPacket(player, new SM_PRIVATE_STORE(getOwner().getStore(), player));
				break;
			case QUEST_ACCEPT_1:
			case QUEST_ACCEPT_SIMPLE:
				if (!getOwner().equals(player) && PositionUtil.isInRange(getOwner(), player, 100)) { // TODO check if owner really shared
					if (!DataManager.QUEST_DATA.getQuestById(questId).isCannotShare())
						QuestService.startQuest(new QuestEnv(null, player, questId, dialogActionId));
				}
				break;
		}
	}

	public void onLevelChange(int oldLevel, int newLevel) {
		if (oldLevel == newLevel)
			return;

		Player player = getOwner();
		int minNewLevel = oldLevel < newLevel ? oldLevel + 1 : oldLevel - 1; // for skill learning and other stuff that only wants the new level(s)

		if (GSConfig.ENABLE_RATIO_LIMITATION
			&& (player.getAccount().getNumberOf(player.getRace()) == 1 || player.getAccount().getMaxPlayerLevel() == newLevel)) {
			if (oldLevel < GSConfig.RATIO_MIN_REQUIRED_LEVEL && newLevel >= GSConfig.RATIO_MIN_REQUIRED_LEVEL)
				GameServer.updateRatio(player.getRace(), 1);
			else if (oldLevel >= GSConfig.RATIO_MIN_REQUIRED_LEVEL && newLevel < GSConfig.RATIO_MIN_REQUIRED_LEVEL)
				GameServer.updateRatio(player.getRace(), -1);
		}

		player.getGameStats().updateStatsTemplate();
		player.getCommonData().updateMaxRepose();
		player.getCommonData().resetSalvationPoints();
		upgradePlayer();
		PacketSendUtility.broadcastPacket(player, new SM_ACTION_ANIMATION(player.getObjectId(), ActionAnimation.LEVEL_UP, newLevel), true);

		player.getNpcFactions().onLevelUp();
		QuestEngine.getInstance().onLevelChanged(player);
		updateNearbyQuests();
		if (HTMLConfig.ENABLE_GUIDES && player.isSpawned())
			HTMLService.sendGuideHtml(player, minNewLevel, newLevel);
		SkillLearnService.learnNewSkills(player, minNewLevel, newLevel);
		BonusPackService.getInstance().addPlayerCustomReward(player);
		FactionPackService.getInstance().addPlayerCustomReward(player);
		if (CustomConfig.ENABLE_STARTER_KIT)
			StarterKitService.getInstance().onLevelUp(player, minNewLevel, newLevel);
	}

	public void upgradePlayer() {
		Player player = getOwner();
		player.getLifeStats().synchronizeWithMaxStats();
		player.getGameStats().updateStatsVisually();

		if (player.isInTeam() && !TeamStatUpdater.getInstance().hasTask(player)) // SM_GROUP_MEMBER_INFO / SM_ALLIANCE_MEMBER_INFO task
			TeamStatUpdater.getInstance().startTask(player);

		if (player.isLegionMember()) // SM_LEGION_UPDATE_MEMBER
			LegionService.getInstance().updateMemberInfo(player);
	}

	public void onChangedPlayerAttributes() {
		getOwner().clearKnownlist();
		PacketSendUtility.sendPacket(getOwner(), new SM_PLAYER_INFO(getOwner()));
		PacketSendUtility.sendPacket(getOwner(), new SM_MOTION(getOwner().getObjectId(), getOwner().getMotions().getActiveMotions()));
		if (getOwner().isInPlayerMode(PlayerMode.RIDE))
			PacketSendUtility.sendPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.RIDE, 0, getOwner().ride.getNpcId()));
		if (getOwner().getSeeState() != 0)
			PacketSendUtility.sendPacket(getOwner(), new SM_PLAYER_STATE(getOwner())); // needed to see hidden creatures again
		getOwner().getEffectController().updatePlayerEffectIcons(null);
		getOwner().updateKnownlist();
	}

	/**
	 * After entering game player char is "blinking" which means that it's in under some protection, after making an action char stops blinking. -
	 * Starts protection active - Schedules task to end protection
	 */
	public void startProtectionActiveTask() {
		if (!getOwner().isProtectionActive()) {
			getOwner().setVisualState(CreatureVisualState.BLINKING);
			AttackUtil.cancelCastOn(getOwner());
			AttackUtil.removeTargetFrom(getOwner());
			PacketSendUtility.broadcastToSightedPlayers(getOwner(), new SM_PLAYER_STATE(getOwner()), true);
			addTask(TaskId.PROTECTION_ACTIVE, ThreadPoolManager.getInstance().schedule(this::stopProtectionActiveTask, 60000));
		}
	}

	/**
	 * Stops protection active task after first move or use skill
	 */
	public void stopProtectionActiveTask() {
		cancelTask(TaskId.PROTECTION_ACTIVE);
		Player player = getOwner();
		if (player.isSpawned()) {
			player.unsetVisualState(CreatureVisualState.BLINKING);
			PacketSendUtility.broadcastToSightedPlayers(player, new SM_PLAYER_STATE(player), true);
			notifyAIOnMove();
		}
	}

	/**
	 * When player arrives at destination point of flying teleport
	 */
	public void onFlyTeleportEnd() {
		Player player = getOwner();
		if (player.isInPlayerMode(PlayerMode.WINDSTREAM)) {
			player.unsetPlayerMode(PlayerMode.WINDSTREAM);
			player.getLifeStats().triggerFpReduce();
			player.unsetState(CreatureState.FLYING);
			player.unsetFlyState(FlyState.FLYING);
			player.setFlyState(FlyState.GLIDING);
			player.setState(CreatureState.ACTIVE);
			player.setState(CreatureState.GLIDING);
			player.getGameStats().updateStatsAndSpeedVisually();
		} else {
			player.unsetState(CreatureState.FLYING);
			player.setFlightTeleportId(0);

			if (SecurityConfig.ENABLE_FLYPATH_VALIDATOR) {
				long diff = (System.currentTimeMillis() - player.getFlyStartTime());
				FlyPathEntry path = player.getCurrentFlyPath();

				if (player.getWorldId() != path.getEndWorldId()) {
					AuditLogger.log(player, "tried to use flyPath #" + path.getId() + " from not native start world " + player.getWorldId() + " (expected "
						+ path.getEndWorldId() + ")");
				}

				if (diff < path.getTimeInMs()) {
					AuditLogger.log(player, "ended fly path too early: Fly duration " + diff + "ms instead of " + path.getTimeInMs() + "ms");
					/*
					 * todo if works teleport player to start_* xyz, or even ban
					 */
				}

				player.setCurrentFlypath(null);
			}

			player.setFlightDistance(0);
			player.setState(CreatureState.ACTIVE);
			updateZone();
		}
	}

	public void startStance(int skillId) {
		stopStance();
		stanceObserver = new StanceObserver(getOwner(), skillId);
		getOwner().getObserveController().addObserver(stanceObserver);
	}

	public void stopStance() {
		if (stanceObserver != null) {
			getOwner().getObserveController().removeObserver(stanceObserver);
			getOwner().getEffectController().removeEffect(stanceObserver.getStanceSkillId());
			PacketSendUtility.sendPacket(getOwner(), new SM_PLAYER_STANCE(getOwner(), 0));
			stanceObserver = null;
		}
	}

	public int getStanceSkillId() {
		return stanceObserver == null ? 0 : stanceObserver.getStanceSkillId();
	}

	public boolean isUnderStance() {
		return stanceObserver != null;
	}

	public void updateSoulSickness(int skillId) {
		Player player = getOwner();
		House house = player.getActiveHouse();
		if (house != null)
			switch (house.getHouseType()) {
				case MANSION:
				case ESTATE:
				case PALACE:
					return;
			}

		if (!player.hasPermission(MembershipConfig.DISABLE_SOULSICKNESS)) {
			int deathCount = player.getCommonData().getDeathCount();
			if (deathCount < 10) {
				deathCount++;
				player.getCommonData().setDeathCount(deathCount);
			}

			if (skillId == 0)
				skillId = 8291;
			SkillEngine.getInstance().getSkill(player, skillId, deathCount, player).useSkill();
		}
	}

	/**
	 * Player is considered in combat if he's been attacked or has attacked less or equal 10s before
	 * 
	 * @return true if the player is actively in combat
	 */
	public boolean isInCombat() {
		return System.currentTimeMillis() - getLastCombatTime() <= 10000;
	}

	/**
	 * @return The last time, when the player attacked someone or got attacked
	 */
	public long getLastCombatTime() {
		return Math.max(lastAttackedMillis, lastAttackMillis);
	}

}
