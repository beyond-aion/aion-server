package com.aionemu.gameserver.controllers;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;

import javolution.util.FastMap;
import javolution.util.FastTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.HTMLConfig;
import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.PlayerInitialData;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Kisk;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.PetAction;
import com.aionemu.gameserver.model.gameobjects.PetEmote;
import com.aionemu.gameserver.model.gameobjects.StaticObject;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.BindPointPosition;
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
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABNORMAL_EFFECT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE_HOUSE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE_HOUSE_OBJECT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GATHERABLE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_OBJECT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_RENDER;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_KISK_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEVEL_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_NEARBY_QUESTS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_NPC_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PET;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PET_EMOTE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STANCE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PRIVATE_STORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_REPEAT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_CANCEL;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.restrictions.RestrictionsManager;
import com.aionemu.gameserver.services.BonusPackService;
import com.aionemu.gameserver.services.DuelService;
import com.aionemu.gameserver.services.FactionPackService;
import com.aionemu.gameserver.services.HTMLService;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.PvpService;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.SerialKillerService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.SkillLearnService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.summons.SummonsService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.DispelCategoryType;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.Skill.SkillMethod;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.taskmanager.tasks.PlayerMoveTaskManager;
import com.aionemu.gameserver.taskmanager.tasks.TeamEffectUpdater;
import com.aionemu.gameserver.taskmanager.tasks.TeamMoveUpdater;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.world.MapRegion;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldType;
import com.aionemu.gameserver.world.geo.GeoService;
import com.aionemu.gameserver.world.knownlist.KnownList;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * This class is for controlling players.
 * 
 * @author -Nemesiss-, ATracer, xavier, Sarynth, RotO, xTz, KID, Sippolo
 */
public class PlayerController extends CreatureController<Player> {

	private static Logger log = LoggerFactory.getLogger(PlayerController.class);
	private long lastAttackMillis = 0;
	private long lastAttackedMillis = 0;
	private int stance = 0;

	@Override
	public void see(VisibleObject object) {
		super.see(object);
		if (object instanceof Creature) {
			Creature creature = (Creature) object;
			if (creature instanceof Npc) {
				Npc npc = (Npc) creature;
				PacketSendUtility.sendPacket(getOwner(), new SM_NPC_INFO(npc));
				if (npc instanceof Kisk) {
					if (getOwner().getRace() == ((Kisk) npc).getOwnerRace())
						PacketSendUtility.sendPacket(getOwner(), new SM_KISK_UPDATE((Kisk) npc));
				} else {
					QuestEngine.getInstance().onAtDistance(new QuestEnv(npc, getOwner(), 0, 0));
				}
			} else if (creature instanceof Player) {
				Player player = (Player) creature;
				PacketSendUtility.sendPacket(getOwner(), new SM_PLAYER_INFO(player, getOwner().isAggroIconTo(player)));
				Pet pet = player.getPet();
				if (pet != null) { // pet must spawn after player, otherwise it will not be displayed
					if (!getOwner().getKnownList().knows(pet)) // update pos + knownlist, since client sends pet position only every 50m
						World.getInstance().updatePosition(pet, player.getX(), player.getY(), player.getZ(), player.getHeading(), true);
					else
						getOwner().getKnownList().addVisualObject(pet);
				}
				PacketSendUtility.sendPacket(getOwner(), new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
				if (player.isInPlayerMode(PlayerMode.RIDE))
					PacketSendUtility.sendPacket(getOwner(), new SM_EMOTION(player, EmotionType.RIDE, 0, player.ride.getNpcId()));
			} else if (creature instanceof Summon) {
				PacketSendUtility.sendPacket(getOwner(), new SM_NPC_INFO((Summon) creature));
			}
			if (!creature.getEffectController().isEmpty())
				PacketSendUtility.sendPacket(getOwner(), new SM_ABNORMAL_EFFECT(creature));
		} else if (object instanceof Gatherable || object instanceof StaticObject) {
			PacketSendUtility.sendPacket(getOwner(), new SM_GATHERABLE_INFO(object));
		} else if (object instanceof Pet) {
			Pet pet = (Pet) object;
			PacketSendUtility.sendPacket(getOwner(), new SM_PET(PetAction.SPAWN, pet));
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
			PacketSendUtility.sendPacket(getOwner(), new SM_PET((Pet) object, animation));
		} else if (object instanceof House) {
			PacketSendUtility.sendPacket(getOwner(), new SM_DELETE_HOUSE(((House) object).getAddress().getId()));
		} else if (object instanceof HouseObject) {
			PacketSendUtility.sendPacket(getOwner(), new SM_DELETE_HOUSE_OBJECT(object.getObjectId()));
		} else {
			PacketSendUtility.sendPacket(getOwner(), new SM_DELETE(object, animation));
		}
	}

	/**
	 * Removes owner from the visualObjects lists of all known players who can't see him anymore.
	 */
	public void onHide() {
		Pet pet = getOwner().getPet();
		getOwner().getKnownList().doOnAllPlayers(other -> {
			KnownList knownList = other.getKnownList();
			if (knownList.getVisiblePlayers().containsKey(getOwner().getObjectId()) && !other.canSee(getOwner())) {
				knownList.delVisualObject(getOwner(), ObjectDeleteAnimation.DELAYED);
				if (pet != null)
					knownList.delVisualObject(pet, ObjectDeleteAnimation.NONE);
			}
		});
	}

	/**
	 * Re-adds owner to the visualObjects lists of all known players.
	 */
	public void onHideEnd() {
		Pet pet = getOwner().getPet();
		if (pet != null && !MathUtil.isIn3dRange(getOwner(), pet, 3)) // client sends pet position only every 50m...
			pet.getPosition().setXYZH(getOwner().getX(), getOwner().getY(), getOwner().getZ(), getOwner().getHeading());
		getOwner().getKnownList().doOnAllPlayers(other -> {
			KnownList knownList = other.getKnownList();
			knownList.addVisualObject(getOwner());
			if (pet != null)
				knownList.addVisualObject(pet);
		});
	}

	public void updateNearbyQuests() {
		MapRegion region = getOwner().getPosition().getMapRegion();
		if (region == null) // usually when player isn't spawned yet
			return;
		Map<Integer, Integer> nearbyQuestList = new FastMap<>();
		for (int questId : region.getParent().getQuestIds()) {
			// QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
			// if (template.isTimeBased())
			// continue;
			int diff = 0;
			if (questId <= 0xFFFF)
				diff = QuestService.getLevelRequirementDiff(questId, getOwner().getCommonData().getLevel());
			if (diff <= 2 && QuestService.checkStartConditions(new QuestEnv(null, getOwner(), questId, 0), false))
				nearbyQuestList.put(questId, diff);
		}
		PacketSendUtility.sendPacket(getOwner(), new SM_NEARBY_QUESTS(nearbyQuestList));
	}

	public void updateRepeatableQuests() {
		List<Integer> reapeatQuestList = new FastTable<>();
		for (int questId : getOwner().getPosition().getMapRegion().getParent().getQuestIds()) {
			QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
			if (!template.isTimeBased())
				continue;
			int diff = 0;
			if (questId <= 0xFFFF)
				diff = QuestService.getLevelRequirementDiff(questId, getOwner().getCommonData().getLevel());
			if (diff <= 2 && QuestService.checkStartConditions(new QuestEnv(null, getOwner(), questId, 0), false)) {
				reapeatQuestList.add(questId);
			}
		}
		if (reapeatQuestList.size() > 0)
			PacketSendUtility.sendPacket(getOwner(), new SM_QUEST_REPEAT(reapeatQuestList));
	}

	@Override
	public void onEnterZone(ZoneInstance zone) {
		Player player = getOwner();
		if (!zone.canRide() && player.isInPlayerMode(PlayerMode.RIDE))
			player.unsetPlayerMode(PlayerMode.RIDE);
		SerialKillerService.getInstance().onEnterZone(player, zone);
		InstanceService.onEnterZone(player, zone);
		ZoneName zoneName = zone.getAreaTemplate().getZoneName();
		if (zoneName == null)
			log.error("No name found for a Zone in the map " + zone.getAreaTemplate().getWorldId());
		else if (!zone.hasQuestZoneHandlers()) // Zone handlers should notify QuestEngine directly
			QuestEngine.getInstance().onEnterZone(new QuestEnv(null, player, 0, 0), zone.getAreaTemplate().getZoneName());
	}

	@Override
	public void onLeaveZone(ZoneInstance zone) {
		Player player = getOwner();
		SerialKillerService.getInstance().onLeaveZone(player, zone);
		InstanceService.onLeaveZone(player, zone);
		ZoneName zoneName = zone.getAreaTemplate().getZoneName();
		if (zoneName == null)
			log.error("No name found for a Zone in the map " + zone.getAreaTemplate().getWorldId());
		else
			QuestEngine.getInstance().onLeaveZone(new QuestEnv(null, player, 0, 0), zoneName);
	}

	/**
	 * {@inheritDoc} Should only be triggered from one place (life stats)
	 */
	// TODO [AT] move
	public void onEnterWorld() {
		InstanceService.onEnterInstance(getOwner());
		if (getOwner().getPosition().getWorldMapInstance().getParent().isExceptBuff()) {
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

	// TODO [AT] move
	public void onLeaveWorld() {
		SerialKillerService.getInstance().onLeaveMap(getOwner());
		InstanceService.onLeaveInstance(getOwner());
	}

	public void validateLoginZone() {
		int mapId;
		float x, y, z;
		byte h;
		boolean moveToBind = false;

		BindPointPosition bind = getOwner().getBindPoint();

		if (bind != null) {
			mapId = bind.getMapId();
			x = bind.getX();
			y = bind.getY();
			z = bind.getZ();
			h = bind.getHeading();
		} else {
			PlayerInitialData.LocationData start = DataManager.PLAYER_INITIAL_DATA.getSpawnLocation(getOwner().getRace());

			mapId = start.getMapId();
			x = start.getX();
			y = start.getY();
			z = start.getZ();
			h = start.getHeading();
		}
		if (!SiegeService.getInstance().validateLoginZone(getOwner()))
			moveToBind = true;

		if (moveToBind)
			World.getInstance().setPosition(getOwner(), mapId, x, y, z, h);
	}

	public void onDie(@Nonnull Creature lastAttacker) {
		Player player = this.getOwner();
		player.getController().cancelCurrentSkill(null);
		player.setRebirthRevive(getOwner().haveSelfRezEffect());
		Creature master = lastAttacker.getMaster();

		if (DuelService.getInstance().isDueling(player.getObjectId())) {
			if (master != null && DuelService.getInstance().isDueling(player.getObjectId(), master.getObjectId())) {
				DuelService.getInstance().loseDuel(player);
				player.getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.DEBUFF);
				if (player.getLifeStats().getHpPercentage() < 33)
					player.getLifeStats().setCurrentHpPercent(33);
				if (player.getLifeStats().getMpPercentage() < 33)
					player.getLifeStats().setCurrentMpPercent(33);
				if (master.getLifeStats().getHpPercentage() < 33)
					master.getLifeStats().setCurrentHpPercent(33);
				if (master.getLifeStats().getMpPercentage() < 33)
					master.getLifeStats().setCurrentMpPercent(33);
				return;
			}
			DuelService.getInstance().loseDuel(player);
		}

		/**
		 * Release summon
		 */
		Summon summon = player.getSummon();
		if (summon != null) {
			SummonsService.doMode(SummonMode.RELEASE, summon, UnsummonType.UNSPECIFIED);
		}

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

		// Effects removed with super.onDie()
		super.onDie(lastAttacker);

		if (player.isInInstance() && player.getPosition().getWorldMapInstance().getInstanceHandler().onDie(player, lastAttacker))
			return;

		MapRegion mapRegion = player.getPosition().getMapRegion();
		if (mapRegion != null && mapRegion.onDie(lastAttacker, getOwner()))
			return;

		doReward();

		if (master instanceof Npc || master.equals(player)) {
			if (player.getLevel() > 4 && !isNoDeathPenaltyInEffect())
				player.getCommonData().calculateExpLoss();
		}

		sendDieFromCreature(lastAttacker, !player.hasResurrectBase());

		QuestEngine.getInstance().onDie(new QuestEnv(null, player, 0, 0));
	}

	public void sendDie() {
		sendDieFromCreature(getOwner(), true);
	}

	private void sendDieFromCreature(@Nonnull Creature lastAttacker, boolean showPacket) {
		Player player = getOwner();
		if (player.getPanesterraTeam() != null && player.getWorldId() == 400030000) {
			PacketSendUtility.sendPacket(player, new SM_DIE(player.canUseRebirthRevive(), player.haveSelfRezItem(), 0, 6));
		} else if (showPacket) {
			int kiskTimeRemaining = (player.getKisk() != null ? player.getKisk().getRemainingLifetime() : 0);
			if (player.getSKInfo().getRank() > 1)
				kiskTimeRemaining = 0;
			PacketSendUtility.sendPacket(player,
				new SM_DIE(player.canUseRebirthRevive(), player.haveSelfRezItem(), kiskTimeRemaining, 0, isInvader(player)));
		}
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
		if (getOwner().getLifeStats().isAlreadyDead())
			return;
		if (getOwner().getIsFlyingBeforeDeath())
			getOwner().unsetState(CreatureState.FLOATING_CORPSE);
		else
			getOwner().unsetState(CreatureState.DEAD);
		getOwner().setState(CreatureState.ACTIVE);
	}

	@Override
	public void attackTarget(Creature target, int time, boolean skipChecks) {

		PlayerGameStats gameStats = getOwner().getGameStats();

		if (!RestrictionsManager.canAttack(getOwner(), target))
			return;

		// Normal attack is already limited client side (ex. Press C and attacker approaches target)
		// but need a check server side too also for Z axis issue
		if (!MathUtil.isInAttackRange(getOwner(), target, getOwner().getGameStats().getAttackRange().getCurrent() / 1000f + 1))
			return;

		if (!GeoService.getInstance().canSee(getOwner(), target)) {
			PacketSendUtility.sendPacket(getOwner(), SM_SYSTEM_MESSAGE.STR_ATTACK_OBSTACLE_EXIST());
			return;
		}

		if (target instanceof Npc) {
			QuestEngine.getInstance().onAttack(new QuestEnv(target, getOwner(), 0, 0));
		}

		int attackSpeed = gameStats.getAttackSpeed().getCurrent();

		long milis = System.currentTimeMillis();
		// network ping..
		if (milis - lastAttackMillis + 300 < attackSpeed) {
			// hack
			return;
		}
		lastAttackMillis = milis;

		/**
		 * notify attack observers
		 */
		super.attackTarget(target, time, true);

	}

	@Override
	public void onAttack(Creature creature, int skillId, TYPE type, int damage, boolean notifyAttack, LOG logId, AttackStatus attackStatus) {
		if (getOwner().getLifeStats().isAlreadyDead())
			return;

		if (getOwner().isProtectionActive())
			damage = 0;

		cancelUseItem();
		cancelGathering();
		super.onAttack(creature, skillId, type, damage, notifyAttack, logId, attackStatus);

		if (creature instanceof Npc) {
			QuestEngine.getInstance().onAttack(new QuestEnv(creature, getOwner(), 0, 0));
		}

		lastAttackedMillis = System.currentTimeMillis();
	}

	/**
	 * @param skillId
	 * @param targetType
	 * @param x
	 * @param y
	 * @param z
	 */
	public void useSkill(int skillId, int targetType, float x, float y, float z, int time) {
		Player player = getOwner();

		Skill skill = SkillEngine.getInstance().getSkillFor(player, skillId, player.getTarget());

		if (skill != null) {
			if (!RestrictionsManager.canUseSkill(player, skill))
				return;

			skill.setTargetType(targetType, x, y, z);
			skill.setHitTime(time);
			skill.useSkill();
		}
	}

	/**
	 * @param template
	 * @param targetType
	 * @param x
	 * @param y
	 * @param z
	 * @param clientHitTime
	 */
	public void useSkill(SkillTemplate template, int targetType, float x, float y, float z, int clientHitTime, int skillLevel) {
		Player player = getOwner();
		Skill skill = null;
		skill = SkillEngine.getInstance().getSkillFor(player, template, player.getTarget());
		if (skill == null && player.isTransformed()) {
			SkillPanel panel = DataManager.PANEL_SKILL_DATA.getSkillPanel(player.getTransformModel().getPanelId());
			if (panel != null && panel.canUseSkill(template.getSkillId(), skillLevel)) {
				skill = SkillEngine.getInstance().getSkillFor(player, template, player.getTarget(), skillLevel);
			}
		}

		if (skill != null) {
			if (!RestrictionsManager.canUseSkill(player, skill))
				return;

			skill.setTargetType(targetType, x, y, z);
			skill.setHitTime(clientHitTime);
			skill.useSkill();
		}
	}

	@Override
	public void onMove() {
		super.onMove();
		if (getOwner().isInTeam())
			TeamMoveUpdater.getInstance().startTask(getOwner());
	}

	@Override
	public void onStopMove() {
		PlayerMoveTaskManager.getInstance().removePlayer(getOwner());
		getOwner().getMoveController().setInMove(false);
		cancelCurrentSkill(null);
		updateZone();
		super.onStopMove();
	}

	@Override
	public void onStartMove() {
		getOwner().getMoveController().setInMove(true);
		PlayerMoveTaskManager.getInstance().addPlayer(getOwner());
		cancelUseItem();
		cancelCurrentSkill(null);
		super.onStartMove();
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
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED(new DescriptionId(castingSkill.getItemTemplate().getNameId())));
			player.removeItemCoolDown(castingSkill.getItemTemplate().getUseLimits().getDelayId());
			PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), castingSkill.getFirstTarget().getObjectId(),
				castingSkill.getItemObjectId(), castingSkill.getItemTemplate().getTemplateId(), 0, 3, 0), true);
		}

		if (lastAttacker != null && lastAttacker instanceof Player && !lastAttacker.equals(getOwner())) {
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
		if (player.getTarget() instanceof Gatherable) {
			Gatherable g = (Gatherable) player.getTarget();
			g.getController().finishGathering(player);
		}
	}

	@Override
	public Player getOwner() {
		return (Player) super.getOwner();
	}

	/**
	 * @param player
	 * @return
	 */
	// TODO [AT] move to Player
	public boolean isDueling(Player player) {
		return DuelService.getInstance().isDueling(player.getObjectId(), getOwner().getObjectId());
	}

	@Override
	public void onDialogSelect(int dialogId, int prevDialogId, Player player, int questId, int extendedRewardIndex) {
		switch (dialogId) {
			case 2:
				PacketSendUtility.sendPacket(player, new SM_PRIVATE_STORE(getOwner().getStore(), player));
				break;
		}
	}

	public void onLevelChange(int oldLevel, int newLevel) {
		if (oldLevel == newLevel)
			return;

		Player player = getOwner();
		int minNewLevel = oldLevel < newLevel ? oldLevel + 1 : oldLevel - 1; // for skill learning and other stuff that only wants the new level(s)

		if (GSConfig.ENABLE_RATIO_LIMITATION
			&& (player.getPlayerAccount().getNumberOf(player.getRace()) == 1 || player.getPlayerAccount().getMaxPlayerLevel() == newLevel)) {
			if (oldLevel < GSConfig.RATIO_MIN_REQUIRED_LEVEL && newLevel >= GSConfig.RATIO_MIN_REQUIRED_LEVEL)
				GameServer.updateRatio(player.getRace(), 1);
			else if (oldLevel >= GSConfig.RATIO_MIN_REQUIRED_LEVEL && newLevel < GSConfig.RATIO_MIN_REQUIRED_LEVEL)
				GameServer.updateRatio(player.getRace(), -1);
		}

		player.getCommonData().updateMaxRepose();
		player.getCommonData().resetSalvationPoints();
		upgradePlayer();
		PacketSendUtility.broadcastPacket(player, new SM_LEVEL_UPDATE(player.getObjectId(), 0, newLevel), true);

		player.getNpcFactions().onLevelUp();
		QuestEngine.getInstance().onLvlUp(new QuestEnv(null, player, 0, 0));
		updateNearbyQuests();
		if (HTMLConfig.ENABLE_GUIDES && player.isSpawned())
			HTMLService.sendGuideHtml(player, minNewLevel, newLevel);
		SkillLearnService.learnNewSkills(player, minNewLevel, newLevel);
		BonusPackService.getInstance().addPlayerCustomReward(player);
		FactionPackService.getInstance().addPlayerCustomReward(player);
	}

	public void upgradePlayer() {
		Player player = getOwner();
		player.getLifeStats().synchronizeWithMaxStats();
		player.getGameStats().updateStatsVisually();

		if (player.isInTeam()) // SM_GROUP_MEMBER_INFO / SM_ALLIANCE_MEMBER_INFO task
			TeamEffectUpdater.getInstance().startTask(player);

		if (player.isLegionMember()) // SM_LEGION_UPDATE_MEMBER
			LegionService.getInstance().updateMemberInfo(player);
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
			PacketSendUtility.broadcastPacket(getOwner(), new SM_PLAYER_STATE(getOwner()), true);
			Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					stopProtectionActiveTask();
				}

			}, 60000);
			addTask(TaskId.PROTECTION_ACTIVE, task);
		}
	}

	/**
	 * Stops protection active task after first move or use skill
	 */
	public void stopProtectionActiveTask() {
		cancelTask(TaskId.PROTECTION_ACTIVE);
		Player player = getOwner();
		if (player != null && player.isSpawned()) {
			player.unsetVisualState(CreatureVisualState.BLINKING);
			PacketSendUtility.broadcastPacket(player, new SM_PLAYER_STATE(player), true);
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
			player.unsetState(CreatureState.FLIGHT_TELEPORT);
			player.setFlightTeleportId(0);

			if (SecurityConfig.ENABLE_FLYPATH_VALIDATOR) {
				long diff = (System.currentTimeMillis() - player.getFlyStartTime());
				FlyPathEntry path = player.getCurrentFlyPath();

				if (player.getWorldId() != path.getEndWorldId()) {
					AuditLogger.info(player, "Player tried to use flyPath #" + path.getId() + " from not native start world " + player.getWorldId()
						+ ". expected " + path.getEndWorldId());
				}

				if (diff < path.getTimeInMs()) {
					AuditLogger.info(player, "Player " + player.getName() + " used flypath bug " + diff + " instead of " + path.getTimeInMs());
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

	public boolean addItems(int itemId, int count) {
		return ItemService.addQuestItems(getOwner(), Collections.singletonList(new QuestItems(itemId, count)));
	}

	public void startStance(final int skillId) {
		stance = skillId;
		if (skillId != 0) {
			getOwner().getObserveController().attach(new ActionObserver(ObserverType.ABNORMALSETTED) {

				@Override
				public void abnormalsetted(AbnormalState state) {
					if ((state.getId() & AbnormalState.STANCE_OFF.getId()) != 0)
						stopStance();
				}
			});
		}
	}

	public void stopStance() {
		getOwner().getEffectController().removeEffect(stance);
		PacketSendUtility.sendPacket(getOwner(), new SM_PLAYER_STANCE(getOwner(), 0));
		stance = 0;
	}

	public int getStanceSkillId() {
		return stance;
	}

	public boolean isUnderStance() {
		return stance != 0;
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

		if (!player.havePermission(MembershipConfig.DISABLE_SOULSICKNESS)) {
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
		return (((System.currentTimeMillis() - lastAttackedMillis) <= 10000) || ((System.currentTimeMillis() - lastAttackMillis) <= 10000));
	}

	/**
	 * Check if NoDeathPenalty is active
	 * 
	 * @return boolean
	 */
	public boolean isNoDeathPenaltyInEffect() {
		// Check if NoDeathPenalty is active
		Iterator<Effect> iterator = getOwner().getEffectController().iterator();
		while (iterator.hasNext()) {
			Effect effect = iterator.next();
			if (effect.isNoDeathPenalty())
				return true;
		}
		return false;
	}

	/**
	 * Check if NoResurrectPenalty is active
	 * 
	 * @return boolean
	 */
	public boolean isNoResurrectPenaltyInEffect() {
		// Check if NoResurrectPenalty is active
		Iterator<Effect> iterator = getOwner().getEffectController().iterator();
		while (iterator.hasNext()) {
			Effect effect = iterator.next();
			if (effect.isNoResurrectPenalty())
				return true;
		}
		return false;
	}

	/**
	 * Check if HiPass is active
	 * 
	 * @return boolean
	 */
	public boolean isHiPassInEffect() {
		// Check if HiPass is active
		Iterator<Effect> iterator = getOwner().getEffectController().iterator();
		while (iterator.hasNext()) {
			Effect effect = iterator.next();
			if (effect.isHiPass())
				return true;
		}
		return false;
	}

}
