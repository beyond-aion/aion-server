package com.aionemu.gameserver.skillengine.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.ai.handler.ShoutEventHandler;
import com.aionemu.gameserver.ai.manager.SkillAttackManager;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.controllers.observer.StartMovingListener;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.abyss.AbyssService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.action.Action;
import com.aionemu.gameserver.skillengine.action.Actions;
import com.aionemu.gameserver.skillengine.condition.Conditions;
import com.aionemu.gameserver.skillengine.condition.SkillChargeCondition;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.properties.FirstTargetAttribute;
import com.aionemu.gameserver.skillengine.properties.Properties;
import com.aionemu.gameserver.skillengine.properties.Properties.CastState;
import com.aionemu.gameserver.skillengine.properties.TargetRangeAttribute;
import com.aionemu.gameserver.skillengine.properties.TargetRelationAttribute;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer, Wakizashi, Neon
 */
public class Skill {

	private static final Logger log = LoggerFactory.getLogger(Skill.class);

	private final List<Creature> effectedList;
	private Creature firstTarget;
	protected final Creature effector;
	private final int skillLevel;
	protected SkillMethod skillMethod;
	protected final StartMovingListener moveListener;
	private final SkillTemplate skillTemplate;
	private boolean firstTargetRangeCheck = true;
	private final ItemTemplate itemTemplate;
	private int itemObjectId = 0;
	private int targetType;
	private boolean chainSuccess = true;
	private boolean isCancelled = false;
	private boolean blockedPenaltySkill = false;
	private float x;
	private float y;
	private float z;
	private byte h;
	private int boostSkillCost;
	/**
	 * Duration that depends on BOOST_CASTING_TIME
	 */
	private int baseCastDuration;
	private int castDuration;
	private int hitTime;// from CM_CASTSPELL
	private int serverTime;// time when effect is applied
	private int animationTime;
	private long castStartTime;
	private boolean instantSkill = false;
	private String chainCategory = null;
	private int chainUsageDuration = 0;
	private volatile boolean isMultiCast = false;
	private float[] chargeTimes;
	private int hate;
	private volatile ActionObserver firstTargetDieObserver;

	public enum SkillMethod {
		CAST,
		ITEM,
		PASSIVE,
		PROVOKED,
		CHARGE,
		PENALTY
	}

	/**
	 * Each skill is a separate object upon invocation Skill level will be populated from player SkillList
	 */
	public Skill(SkillTemplate skillTemplate, Player effector, Creature firstTarget) {
		this(skillTemplate, effector, effector.getSkillList().getSkillLevel(skillTemplate.getSkillId()), firstTarget, null);
	}

	public Skill(SkillTemplate skillTemplate, Player effector, Creature firstTarget, int skillLevel) {
		this(skillTemplate, effector, skillLevel, firstTarget, null);
	}

	public Skill(SkillTemplate skillTemplate, Creature effector, int skillLvl, Creature firstTarget, ItemTemplate itemTemplate) {
		this.effectedList = new ArrayList<>();
		this.moveListener = new StartMovingListener();
		this.firstTarget = firstTarget;
		this.skillLevel = skillLvl;
		this.skillTemplate = skillTemplate;
		this.effector = effector;
		this.baseCastDuration = skillTemplate.getDuration();
		this.castDuration = skillTemplate.getDuration();
		this.itemTemplate = itemTemplate;
		initializeSkillMethod();
	}

	protected void initializeSkillMethod() {
		if (itemTemplate != null)
			skillMethod = SkillMethod.ITEM;
		else if (skillTemplate.isPassive())
			skillMethod = SkillMethod.PASSIVE;
		else if (skillTemplate.isProvoked())
			skillMethod = SkillMethod.PROVOKED;
		else if (skillTemplate.isCharge())
			skillMethod = SkillMethod.CHARGE;
		else
			skillMethod = SkillMethod.CAST;
	}

	/**
	 * Check if the skill can be used
	 *
	 * @return True if the skill can be used
	 */
	public boolean canUseSkill(CastState castState) {
		Properties properties = skillTemplate.getProperties();
		if (properties != null && !properties.validate(this, castState)) {
			log.debug("properties failed");
			return false;
		}

		if (!preCastCheck())
			return false;

		// check for counter skill
		if (effector instanceof Player player) {
			if ((skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.CHARGE) && chainCategory == null) // category gets set in preCastCheck()
				player.getChainSkills().resetChain();

			if (skillTemplate.getCounterSkill() != null) {
				long time = player.getLastCounterSkill(skillTemplate.getCounterSkill());
				if ((time + 5000) < System.currentTimeMillis()) {
					log.debug("chain skill failed, too late");
					return false;
				}
			}

			if (skillMethod == SkillMethod.ITEM && castDuration > 0 && player.getMoveController().isInMove()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED());
				return false;
			}
		}

		return validateEffectedList();
	}

	private boolean validateEffectedList() {
		if (effector instanceof Player player) {
			if (canUseSkill(player))
				effectedList.removeIf(effected -> !isValidTarget(player, effected));
			else
				effectedList.clear();
		}

		if (targetType == 0 && effectedList.isEmpty()) { // target selected but no target will be hit
			if (getTargetRangeAttribute() != TargetRangeAttribute.AREA) { // don't restrict AoE activation
				if (effector instanceof Player player)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID());
				return false;
			}
		}

		return true;
	}

	private boolean canUseSkill(Player player) {
		if (player.isUsingFlyTeleport())
			return false;
		if (!getSkillTemplate().hasEvadeEffect() && player.getEffectController().isInAnyAbnormalState(AbnormalState.CANT_ATTACK_STATE))
			return false;
		if (player.getStore() != null)
			return false;
		return true;
	}

	private boolean isValidTarget(Player player, Creature target) {
		if (target instanceof Player targetPlayer) {
			if (targetPlayer.isUsingFlyTeleport())
				return false;
			if (target.getRace() != player.getRace()) {
				if (!target.isEnemyFrom(player))
					return false;
			} else if (targetPlayer.isDueling(player) && getSkillTemplate().getProperties().getTargetRelation() != TargetRelationAttribute.ENEMY) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID());
				return false;
			}
		}

		if (target.getLifeStats().isAboutToDie() && !isNonTargetAOE())
			return false;

		if (target.isDead() && !getSkillTemplate().hasResurrectEffect() && !isNonTargetAOE()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID());
			return false;
		}

		// cant resurrect non players and non dead
		if (getSkillTemplate().hasResurrectEffect() && (!(target instanceof Player) || !target.isDead()))
			return false;

		return true;
	}

	/**
	 * Skill entry point
	 *
	 * @return true if usage is successful
	 */
	public boolean useSkill() {
		return useSkill(true, true);
	}

	public boolean useNoAnimationSkill() {
		return useSkill(false, true);
	}

	public boolean useWithoutPropSkill() {
		return useSkill(false, false);
	}

	private boolean useSkill(boolean checkAnimation, boolean checkproperties) {
		boostSkillCost = 0;
		effector.getObserveController().notifyBoostSkillCostObservers(this);

		if (checkproperties && !canUseSkill(CastState.CAST_START))
			return false;

		calculateAndSetCastDuration();

		if (SecurityConfig.MOTION_TIME) {
			// must be after calculateskillduration
			if (checkAnimation && !checkAnimationTime()) {
				log.debug("check animation time failed");
				return false;
			}
		}

		// notify skill use observers
		if (skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.ITEM || skillMethod == SkillMethod.CHARGE)
			effector.getObserveController().notifyStartSkillCastObservers(this);

		// start casting
		effector.setCasting(this);

		// send packets to start casting
		if (skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.ITEM || skillMethod == SkillMethod.CHARGE) {
			castStartTime = System.currentTimeMillis();
			startCast();
			if (effector instanceof Npc)
				effector.getAi().setSubStateIfNot(AISubState.CAST);
		}

		effector.getObserveController().attach(moveListener);

		if (effector instanceof Npc npc) {
			NpcSkillEntry currentNpcSkillEntry = npc.getGameStats().getLastSkill();
			if (currentNpcSkillEntry != null) {
				currentNpcSkillEntry.setLastTimeUsed();
				npc.getGameStats().setNextSkillDelay(currentNpcSkillEntry.getNextSkillTime());
			} else {
				npc.getGameStats().setNextSkillDelay(-1);
			}
		}
		effector.getAi().onStartUseSkill(skillTemplate, skillLevel);
		if (castDuration > 0) {
			schedule(castDuration);
		} else {
			endCast();
		}
		return true;
	}

	private void setCooldowns() {
		int cooldown = effector.getSkillCooldown(skillTemplate);
		if (cooldown != 0) {
			if (skillTemplate.getCooldownDeltaLv() != 0)
				cooldown = cooldown + skillTemplate.getCooldownDeltaLv() * skillLevel;
			effector.setSkillCoolDown(skillTemplate.getCooldownId(), cooldown * 100 + System.currentTimeMillis());
			effector.setSkillCoolDownBase(skillTemplate.getCooldownId(), System.currentTimeMillis());
		}
	}

	public int getCooldown() {
		int cooldown = effector.getSkillCooldown(skillTemplate);
		if (cooldown != 0)
			if (skillTemplate.getCooldownDeltaLv() != 0)
				cooldown = cooldown + skillTemplate.getCooldownDeltaLv() * skillLevel;
		return cooldown;
	}

	protected void calculateAndSetCastDuration() {
		// ap & cash revival stones, or 2nd+ time of multicast-skill activation
		if (getSkillId() == 10802
			|| isMulticast() && effector instanceof Player && ((Player) effector).getChainSkills().getCurrentChainCount(chainCategory) > 0) {
			castDuration = 0;
			return;
		}

		if (skillMethod == SkillMethod.CHARGE) {
			SkillChargeCondition chargeCondition = skillTemplate.getSkillChargeCondition();
			if (chargeCondition != null) {
				int maxCastDuration = 0;
				ChargeSkillEntry skillCharge = DataManager.SKILL_CHARGE_DATA.getChargedSkillEntry(chargeCondition.getValue());
				for (ChargedSkill chargedSkill : skillCharge.getSkills())
					maxCastDuration += chargedSkill.getTime();
				castDuration = baseCastDuration = maxCastDuration;
			}
		}

		if (isCastTimeFixed())
			return;

		int boostValue;
		boolean noBaseDurationCap = false;
		boolean isPhysicalCharge = skillMethod == SkillMethod.CHARGE && (effector instanceof Player)
			&& (((Player) effector).getPlayerClass().isPhysicalClass() || ((Player) effector).getPlayerClass() == PlayerClass.RIDER
				|| ((Player) effector).getPlayerClass() == PlayerClass.GUNNER);

		if (skillTemplate.getType() == SkillType.MAGICAL || skillMethod == SkillMethod.CHARGE) {
			if (!isPhysicalCharge) {
				castDuration = effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME, baseCastDuration);
				boostValue = effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME_SKILL, baseCastDuration);
				switch (skillTemplate.getSubType()) {
					case SUMMON:
						boostValue = effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME_SUMMON, boostValue);
						if (effector.getEffectController().hasAbnormalEffect(3779))
							noBaseDurationCap = true;
						break;
					case SUMMONHOMING:
						boostValue = effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME_SUMMONHOMING, boostValue);
						if (effector.getEffectController().hasAbnormalEffect(3779))
							noBaseDurationCap = true;
						break;
					case SUMMONTRAP:
						int tempBoostVal = boostValue;
						boostValue = effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME_TRAP, boostValue);
						if (boostValue == 0 && castDuration < tempBoostVal) {
							boostValue = tempBoostVal - castDuration;
						}
						if (effector.getEffectController().hasAbnormalEffect(913))
							noBaseDurationCap = true;
						break;
					case HEAL:
						boostValue = effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME_HEAL, boostValue);
						break;
					case ATTACK:
						boostValue = effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME_ATTACK, boostValue);
						break;
				}
				castDuration -= baseCastDuration - boostValue;
			} else {
				castDuration = (int) effector.getGameStats().getPositiveStat(StatEnum.ATTACK_SPEED, baseCastDuration);
			}
		}

		// 70% of base skill castDuration cap
		// No cast speed cap for skill Summoning Alacrity I(skillId: 1778) and Nimble Fingers I(skillId: 2386)
		if (!noBaseDurationCap) {
			int baseDurationCap = Math.round(baseCastDuration * 0.3f);
			if (castDuration < baseDurationCap) {
				castDuration = baseDurationCap;
			}
		}

		if (effector instanceof Npc npc) // TODO: check if all skills should be effected
			castDuration = Math.round(baseCastDuration * (npc.getGameStats().getCastSpeed() / 1000f));

		if (castDuration < 0)
			castDuration = 0;
	}

	private boolean checkAnimationTime() {
		if (!(effector instanceof Player player) || skillMethod != SkillMethod.CAST && skillMethod != SkillMethod.CHARGE)// TODO item skills?
			return true;

		if (player.getTransformModel().isActive() && player.getTransformModel().getType() == TransformType.FORM1)
			return true;

		Motion motion = getSkillTemplate().getMotion();
		if (motion == null || motion.getName() == null) // skills like Remove Shock (283) or Feint (912)
			return true; // some skills, like Blind Side (3467) or scroll/food buffs have no motion

		MotionTime motionTime = DataManager.MOTION_DATA.getMotionTime(motion.getName());
		if (motionTime == null) // no warning here (already sent on server startup to avoid permanent spam, see DataManager.SKILL_DATA.validateMotions())
			return true;

		WeaponTypeWrapper weapons = new WeaponTypeWrapper(player.getEquipment().getMainHandWeaponType(), player.getEquipment().getOffHandWeaponType());
		int clientTime = hitTime;
		int serverHitTime = 0;
		int motionId = 1;
		if (isMulticast() && player.getChainSkills().getCurrentChainCount(chainCategory) > 0) {
			motionId = player.getChainSkills().getCurrentChainCount(chainCategory) + 1;
		}
		Times time = motionTime.getTimesFor(player.getRace(), player.getGender(), weapons, player.isInRobotMode(), motionId);
		if (time != null) {
			float atkSpeed = player.getGameStats().getAttackSpeed().getCurrent() / (float) player.getGameStats().getAttackSpeed().getBase();

			float castSpeed = player.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME, 1000) / 1000f;
			if (castSpeed < 0.3f)
				castSpeed = 0.3f;

			float speedModifier = Math.min(atkSpeed, castSpeed);
			animationTime = (int) (time.getMaxTime() * motion.getSpeed() * speedModifier * 10);
			serverHitTime = (int) Math.ceil((player.isInRobotMode() ? time.getMaxTime() : time.getMinTime()) * motion.getSpeed() * speedModifier * 10);
		}

		long ammoTime = 0;
		if (getSkillTemplate().getAmmoSpeed() != 0) {
			double distance = PositionUtil.getDistance(effector, firstTarget);
			ammoTime = Math.round(distance / getSkillTemplate().getAmmoSpeed() * 1000);// checked with client
		}

		int finalTime = Math.round(motion.getDelay() + (serverHitTime + ammoTime) * 0.98f); // client sends rounded times, too

		if (motion.isInstantSkill() && clientTime == 0) {
			this.serverTime = (int) ammoTime;
		} else if (clientTime < finalTime) {
			AuditLogger.log(player, "Modified skill time for client skill: " + getSkillId() + " (clientTime < finalTime: " + clientTime + "/" + finalTime
				+ "). Player is in move: " + player.getMoveController().isInMove());
			this.serverTime = finalTime;
		} else {
			this.serverTime = clientTime;
		}

		if (skillMethod != SkillMethod.CHARGE)
			player.setNextSkillUse(System.currentTimeMillis() + castDuration + (long) (animationTime * 0.8f));

		return true;
	}

	private void startPenaltySkill() {
		int penaltySkill = skillTemplate.getPenaltySkillId();
		if (penaltySkill == 0)
			return;
		if (getSkillTemplate().shouldPenaltySkillSendMsg()) {
			PenaltySkill penaltySkill1 = SkillEngine.getInstance().getPenaltySkill(effector, penaltySkill, 1);
			if (penaltySkill1 != null) {
				penaltySkill1.useSkill();
			}
		} else {
			SkillEngine.getInstance().applyEffectDirectly(penaltySkill, firstTarget, effector);
		}
	}

	/**
	 * Start casting of skill
	 */
	private void startCast() {
		int targetObjId = firstTarget != null ? firstTarget.getObjectId() : 0;
		boolean needsCast = itemTemplate != null && itemTemplate.isCombatActivated();
		float castSpeed = baseCastDuration != 0 ? (float) castDuration / baseCastDuration
			: effector.getGameStats().getReverseStat(StatEnum.BOOST_CASTING_TIME, 1000).getCurrent() / 1000f;
		if (skillMethod == SkillMethod.CHARGE) {
			SkillChargeCondition chargeCondition = skillTemplate.getSkillChargeCondition();
			if (chargeCondition != null) {
				ChargeSkillEntry skillCharge = DataManager.SKILL_CHARGE_DATA.getChargedSkillEntry(chargeCondition.getValue());
				chargeTimes = new float[skillCharge.getSkills().size()];
				for (int i = 0; i < skillCharge.getSkills().size(); i++)
					chargeTimes[i] = skillCharge.getSkills().get(i).getTime() * castSpeed;
			}
		}
		if (skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.CHARGE || needsCast) {
			switch (targetType) {
				case 0: // PlayerObjectId as Target
					PacketSendUtility.broadcastPacketAndReceive(effector,
						new SM_CASTSPELL(effector, skillTemplate.getSkillId(), skillLevel, targetType, targetObjId, castDuration, castSpeed, isMagical()));
					if (effector instanceof Npc) {
						ShoutEventHandler.onCast((NpcAI) effector.getAi(), firstTarget);
					}
					break;

				case 3: // Target not in sight?
					PacketSendUtility.broadcastPacketAndReceive(effector,
						new SM_CASTSPELL(effector, skillTemplate.getSkillId(), skillLevel, targetType, targetObjId, castDuration, castSpeed, isMagical()));
					break;

				case 1: // XYZ as Target
					PacketSendUtility.broadcastPacketAndReceive(effector,
						new SM_CASTSPELL(effector, skillTemplate.getSkillId(), skillLevel, targetType, x, y, z, castDuration, castSpeed, isMagical()));
					break;
			}
		} else if (skillMethod == SkillMethod.ITEM && castDuration > 0) {
			PacketSendUtility.broadcastPacketAndReceive(effector, new SM_ITEM_USAGE_ANIMATION(effector.getObjectId(), firstTarget.getObjectId(),
				itemObjectId, itemTemplate.getTemplateId(), castDuration, 0, 0));
		}

		if (firstTarget != null && !firstTarget.equals(effector) && !skillTemplate.hasResurrectEffect() && (castDuration > 0)
			&& skillTemplate.getProperties().getFirstTarget() != FirstTargetAttribute.POINT
			&& skillTemplate.getProperties().getFirstTarget() != FirstTargetAttribute.ME) {
			if ((effector instanceof Npc && ((Npc) effector).isBoss())
				|| (skillTemplate.getProperties().getFirstTarget() == FirstTargetAttribute.TARGET && skillTemplate.getProperties().getEffectiveDist() > 0)) {
				return;
			}
			firstTargetDieObserver = new ActionObserver(ObserverType.DEATH) {

				@Override
				public void died(Creature creature) {
					getEffector().getController().cancelCurrentSkill(null, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_LOST());
				}
			};
			firstTarget.getObserveController().attach(firstTargetDieObserver);
		}

	}

	/**
	 * Set this skill as canceled
	 */
	public void cancelCast() {
		isCancelled = true;
	}

	/**
	 * Apply effects and perform actions specified in skill template
	 */
	protected void endCast() {
		if (firstTargetDieObserver != null)
			firstTarget.getObserveController().removeObserver(firstTargetDieObserver);
		effector.getObserveController().removeObserver(moveListener);
		if (!effector.isCasting() || isCancelled)
			return;
		// check if target is out of skill range or other requirements are not met (anymore)
		Properties properties = skillTemplate.getProperties();
		if (properties != null && !properties.endCastValidate(this) || !validateEffectedList() || !preUsageCheck()) {
			effector.getController().cancelCurrentSkill(null); // calls effector.setCasting(null) and sends skill cancel packet
			return;
		}
		effector.setCasting(null);

		// try removing item, if its not possible return to prevent exploits
		if (effector instanceof Player && skillMethod == SkillMethod.ITEM) {
			Item item = ((Player) effector).getInventory().getItemByObjId(itemObjectId);
			if (item == null)
				return;
			if (item.getActivationCount() > 1) {
				item.setActivationCount(item.getActivationCount() - 1);
			} else {
				if (!((Player) effector).getInventory().decreaseByObjectId(item.getObjectId(), 1, ItemUpdateType.DEC_ITEM_USE))
					return;
			}
		}

		// set instantSkill, must be before calculate effect
		Motion motion = skillTemplate.getMotion();
		if (motion != null && motion.isInstantSkill() || hitTime == 0)
			instantSkill = true;

		endCondCheck();

		// Perform necessary actions (use mp,dp items etc)
		Actions skillActions = skillTemplate.getActions();
		if (skillActions != null) {
			for (Action action : skillActions.getActions()) {
				if (!action.act(this))
					return;
			}
		}

		// Create effects and precalculate result
		int dashStatus = 0;
		int resistCount = 0;
		boolean blockedChain = false;
		boolean blockedStance = false;
		final List<Effect> effects = new ArrayList<>();
		if (skillTemplate.getEffects() != null) {
			for (Creature effected : effectedList) {
				// TODO: RI_CHARGEATTACK fix: effect is not applied twice, but its Dash effect inflicts (weapon) damage.
				// Seems like Offi is creating a new "Effect" for each DamageEffect. Last one contains relevant info about spell status etc.
				// Client displays the wrong chat output in these cases. (e.g. RI_CHARGEATTACK displays 2x the spellatkinstant damage)
				Effect effect = new Effect(this, effected);
				effect.initialize();
				if (effected instanceof Player) {
					if (effect.getEffectResult() == EffectResult.CONFLICT)
						blockedStance = true;
				}
				final int worldId = effector.getWorldId();
				final int instanceId = effector.getInstanceId();
				effect.setWorldPosition(worldId, instanceId, x, y, z);
				effects.add(effect);
				if (firstTarget == null || firstTarget.equals(effected))
					dashStatus = effect.getDashStatus().getId();
				if (effect.getAttackStatus() == AttackStatus.RESIST || effect.getAttackStatus() == AttackStatus.DODGE) {
					resistCount++;
				}
			}

			if (resistCount == effectedList.size()) {
				blockedChain = true;
				blockedPenaltySkill = true;
			}

			// exception for point point skills(example Ice Sheet)
			if (effectedList.isEmpty()) {
				if (this.isPointPointSkill()) {
					Effect effect = new Effect(this, null);
					effect.initialize();
					effect.setWorldPosition(effector.getWorldId(), effector.getInstanceId(), x, y, z);
					effects.add(effect);
				}
			}
		}

		boolean setCooldowns = true;
		if (effector instanceof Player playerEffector) {
			if (skillTemplate.isStance() && !blockedStance && skillMethod == SkillMethod.CAST)
				playerEffector.getController().startStance(skillTemplate.getSkillId());
			if (isMulticast() && playerEffector.getChainSkills().getCurrentChainCount(chainCategory) > 0)
				setCooldowns = false;

			// Check Chain Skill Trigger Rate, only for chain skills and only for player
			if (chainCategory != null) {
				if (blockedChain)
					chainSuccess = false;
				else
					chainSuccess = Rnd.chance() < skillTemplate.getChainSkillProb() || CustomConfig.SKILL_CHAIN_DISABLE_TRIGGERRATE;

				if (chainSuccess)
					playerEffector.getChainSkills().updateChain(chainCategory, chainUsageDuration);
				else
					playerEffector.getChainSkills().resetChain();
			}

			QuestEngine.getInstance().onUseSkill(new QuestEnv(effector.getTarget(), (Player) effector, 0), skillTemplate.getSkillId());
		}

		if (setCooldowns)
			setCooldowns();

		// Use penalty skill (now 100% success)
		if (!blockedPenaltySkill)
			startPenaltySkill();

		if (instantSkill)
			applyEffect(effects);
		else
			ThreadPoolManager.getInstance().schedule(() -> applyEffect(effects), hitTime);

		if (skillMethod == SkillMethod.PENALTY || skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.ITEM || skillMethod == SkillMethod.CHARGE)
			sendCastspellEnd(dashStatus, effects);

		if (getSkillTemplate().isDeityAvatar() && effector instanceof Player) {
			AbyssService.announceAbyssSkillUsage((Player) effector, getSkillTemplate().getL10n());
		}

		effector.getAi().onEndUseSkill(skillTemplate, skillLevel);
		if (effector instanceof Npc npc) {
			NpcSkillEntry lastSkill = npc.getGameStats().getLastSkill();
			if (lastSkill != null)
				lastSkill.fireOnEndCastEvents(npc);

			SkillAttackManager.afterUseSkill((NpcAI) npc.getAi());
		}

		if (skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.CHARGE) {
			effector.getObserveController().notifyEndSkillCastObservers(this);
		}
		effector.getWorldMapInstance().getInstanceHandler().onEndCastSkill(this);
	}

	private void addResistedEffectHateAndNotifyFriends(List<Effect> effects) {
		if (effects == null || effects.isEmpty()) {
			return;
		}
		effects.stream()
			.filter(
				effect -> effect.getTauntHate() >= 0 && (effect.getAttackStatus() == AttackStatus.RESIST || effect.getAttackStatus() == AttackStatus.DODGE))
			.forEach(effect -> {
				effect.getEffected().getAggroList().addHate(effector, 1);
				effect.getEffected().getKnownList()
					.forEachNpc(object -> object.getAi().onCreatureEvent(AIEventType.CREATURE_NEEDS_SUPPORT, effect.getEffected()));
			});
	}

	private void applyEffect(List<Effect> effects) {
		// Apply effects to effected objects
		effects.forEach(Effect::applyEffect);

		addResistedEffectHateAndNotifyFriends(effects);
	}

	private void sendCastspellEnd(int dashStatus, List<Effect> effects) {
		boolean needsCast = itemTemplate != null && itemTemplate.isCombatActivated();
		AIEventType et = null;
		if (skillMethod == SkillMethod.PENALTY || skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.CHARGE || needsCast) {
			if (this.getSkillTemplate().getSubType() == SkillSubType.ATTACK) {
				et = AIEventType.CREATURE_NEEDS_HELP;
			}
			switch (targetType) {
				case 0: // PlayerObjectId as Target
				case 3: // Target not in sight?
					PacketSendUtility.broadcastPacketAndReceive(effector, new SM_CASTSPELL_RESULT(this, effects, serverTime, chainSuccess, dashStatus), et);
					break;
				case 1: // XYZ as Target
					PacketSendUtility.broadcastPacketAndReceive(effector,
						new SM_CASTSPELL_RESULT(this, effects, serverTime, chainSuccess, dashStatus, targetType), et);
					break;
			}
			if (skillMethod == SkillMethod.ITEM && effector instanceof Player) {
				PacketSendUtility.sendPacket((Player) effector, SM_SYSTEM_MESSAGE.STR_USE_ITEM(getItemTemplate().getL10n()));
			}
		} else if (skillMethod == SkillMethod.ITEM) {

			// TODO: Find out when SM_CASTSPELL_RESULT should be sent with dashStatus = 2, and no SM_ITEM_USAGE_ANIMATION
			PacketSendUtility.broadcastPacketAndReceive(effector,
				new SM_ITEM_USAGE_ANIMATION(effector.getObjectId(), firstTarget.getObjectId(), itemObjectId, itemTemplate.getTemplateId(), 0, 1, 0));
			if (effector instanceof Player)
				PacketSendUtility.sendPacket((Player) effector, SM_SYSTEM_MESSAGE.STR_USE_ITEM(getItemTemplate().getL10n()));
		}
	}

	/**
	 * Schedule actions/effects of skill (channeled skills)
	 */
	private void schedule(int delay) {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isCancelled && skillMethod == SkillMethod.CHARGE) {
				cancelCast();
				effector.setCasting(null);
				if (firstTargetDieObserver != null)
					firstTarget.getObserveController().removeObserver(firstTargetDieObserver);
				PacketSendUtility.broadcastPacketAndReceive(effector, new SM_SKILL_CANCEL(effector, skillTemplate.getSkillId()));
				return;
			}
			endCast();
		}, delay);
	}

	/**
	 * Check all conditions before starting cast
	 */
	private boolean preCastCheck() {
		Conditions skillConditions = skillTemplate.getStartconditions();
		return skillConditions == null || skillConditions.validate(this);
	}

	/**
	 * Check all conditions before using skill
	 */
	private boolean preUsageCheck() {
		Conditions skillConditions = skillTemplate.getUseconditions();
		return skillConditions == null || skillConditions.validate(this);
	}

	/**
	 * Check all conditions after using skill
	 */
	private boolean endCondCheck() {
		Conditions skillConditions = skillTemplate.getEndConditions();
		return skillConditions == null || skillConditions.validate(this);
	}

	/**
	 * @param value
	 *          is the changeMpConsumptionValue to set
	 */
	public void setBoostSkillCost(int value) {
		boostSkillCost = value;
	}

	/**
	 * @return the changeMpConsumptionValue
	 */
	public int getBoostSkillCost() {
		return boostSkillCost;
	}

	/**
	 * @return the effectedList
	 */
	public List<Creature> getEffectedList() {
		return effectedList;
	}

	/**
	 * @return the effector
	 */
	public Creature getEffector() {
		return effector;
	}

	/**
	 * @return the skillLevel
	 */
	public int getSkillLevel() {
		return skillLevel;
	}

	/**
	 * @return the skillId
	 */
	public int getSkillId() {
		return skillTemplate.getSkillId();
	}

	/**
	 * @return the conditionChangeListener
	 */
	public StartMovingListener getMoveListener() {
		return moveListener;
	}

	/**
	 * @return the skillTemplate
	 */
	public SkillTemplate getSkillTemplate() {
		return skillTemplate;
	}

	/**
	 * @return the firstTarget
	 */
	public Creature getFirstTarget() {
		return firstTarget;
	}

	/**
	 * @param firstTarget
	 *          the firstTarget to set
	 */
	public void setFirstTarget(Creature firstTarget) {
		this.firstTarget = firstTarget;
	}

	/**
	 * @return true or false
	 */
	public boolean isPassive() {
		return skillTemplate.getActivationAttribute() == ActivationAttribute.PASSIVE;
	}

	/**
	 * @return the firstTargetRangeCheck
	 */
	public boolean isFirstTargetRangeCheck() {
		return firstTargetRangeCheck;
	}

	public FirstTargetAttribute getFirstTargetAttribute() {
		return skillTemplate.getProperties() == null ? null : skillTemplate.getProperties().getFirstTarget();
	}

	public TargetRangeAttribute getTargetRangeAttribute() {
		return skillTemplate.getProperties() == null ? null : skillTemplate.getProperties().getTargetType();
	}

	/**
	 * @return true if the present skill is a non-targeted, non-point AOE skill
	 */
	public boolean isNonTargetAOE() {
		return getFirstTargetAttribute() == FirstTargetAttribute.ME && getTargetRangeAttribute() == TargetRangeAttribute.AREA;
	}

	/**
	 * @return true if the present skill is a targeted AOE skill
	 */
	private boolean isTargetAOE() {
		return getFirstTargetAttribute() == FirstTargetAttribute.TARGET && getTargetRangeAttribute() == TargetRangeAttribute.AREA;
	}

	/**
	 * @return true if the present skill is a self buff includes items (such as scroll buffs)
	 */
	public boolean isSelfBuff() {
		return getFirstTargetAttribute() == FirstTargetAttribute.ME && getTargetRangeAttribute() == TargetRangeAttribute.ONLYONE
			&& skillTemplate.getSubType() == SkillSubType.BUFF && !skillTemplate.isDeityAvatar();
	}

	/**
	 * @return true if the present skill has self as first target
	 */
	public boolean isFirstTargetSelf() {
		return getFirstTargetAttribute() == FirstTargetAttribute.ME;
	}

	/**
	 * @return true if the present skill is a Point skill
	 */
	public boolean isPointSkill() {
		return getFirstTargetAttribute() == FirstTargetAttribute.POINT;
	}

	/**
	 * @param firstTargetRangeCheck
	 *          the firstTargetRangeCheck to set
	 */
	public void setFirstTargetRangeCheck(boolean firstTargetRangeCheck) {
		this.firstTargetRangeCheck = firstTargetRangeCheck;
	}

	public ItemTemplate getItemTemplate() {
		return itemTemplate;
	}

	public void setItemObjectId(int id) {
		this.itemObjectId = id;
	}

	public int getItemObjectId() {
		return itemObjectId;
	}

	public void setTargetType(int targetType, float x, float y, float z) {
		this.targetType = targetType;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Calculated position after skill
	 */
	public void setTargetPosition(float x, float y, float z, byte h) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.h = h;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public final byte getH() {
		return h;
	}

	/**
	 * @return Returns the time.
	 */
	public int getHitTime() {
		return hitTime;
	}

	public void setHitTime(int time) {
		this.hitTime = time;
	}

	public int getAnimationTime() {
		return animationTime;
	}

	public void setAnimationTime(int animationTime) {
		this.animationTime = animationTime;
	}

	/**
	 * @return true if skill must not be affected by boost casting time this comes from old 1.5.0.5 patch notes and still applies on 2.5 (confirmed)
	 *         TODO: maybe another implementation? At the moment this doesnt seem to be handled on client infos, so it's hard coded
	 */
	private boolean isCastTimeFixed() {
		if (skillMethod != SkillMethod.CAST && skillMethod != SkillMethod.CHARGE) // only casted skills are affected
			return true;

		switch (getSkillId()) {
			case 3775: // Fear
			case 19: // Fear: Poppy
			case 20: // Fear: Ginseng
			case 3589: // Fear Shriek I
			case 1337: // Sleep
			case 17: // Sleep: Scarecrow
			case 18: // Sleep: Frightcorn
			case 1339: // Sleeping Storm
			case 1417: // Curse of Roots
			case 1416: // Curse of Old Roots
			case 1338: // Tranquilizing Cloud I
			case 1340: // Slumberswept Wind
			case 1341: // Slumberswept Wind
			case 1342: // Slumberswept Wind
			case 1343: // Somnolence
			case 1344: // Somnolence
			case 1345: // Somnolence
			case 1956: // Freeze Cannon (3rd charge stage with sleep effect)
			case 11885: // abyss transformation elyos
			case 11886:
			case 11887:
			case 11888:
			case 11889:
			case 11890: // abyss transformation asmo
			case 11891:
			case 11892:
			case 11893:
			case 11894:
			case 243: // Return
			case 245: // Bandage Heal
			case 246: // Herb Treatment
			case 247: // Herb Treatment
			case 249: // MP Recovery
			case 250: // MP Recovery
			case 251: // Herb Treatment
			case 252: // MP Recovery
			case 253: // Herb Treatment
			case 254: // MP Recovery
			case 297: // Herb Treatment
			case 298: // MP Recovery
			case 302: // Escape
			case 308: // Herb Treatment
			case 309: // Herb Treatment
			case 310: // Herb Treatment
			case 311: // Herb Treatment
			case 312: // Herb Treatment
			case 313: // Herb Treatment
			case 314: // Herb Treatment
			case 315: // MP Recovery
			case 316: // MP Recovery
			case 317: // MP Recovery
			case 318: // MP Recovery
			case 319: // MP Recovery
			case 320: // MP Recovery
			case 321: // MP Recovery
				return true;
		}

		return false;
	}

	public void setChainCategory(String chainCategory) {
		this.chainCategory = chainCategory;
	}

	public void setChainUsageDuration(int duration) {
		this.chainUsageDuration = duration;
	}

	public SkillMethod getSkillMethod() {
		return this.skillMethod;
	}

	private boolean isPointPointSkill() {
		return this.getSkillTemplate().getProperties().getFirstTarget() == FirstTargetAttribute.POINT
			&& this.getSkillTemplate().getProperties().getTargetType() == TargetRangeAttribute.POINT;

	}

	private boolean isMulticast() {
		return this.isMultiCast;
	}

	public void setIsMultiCast(boolean isMultiCast) {
		this.isMultiCast = isMultiCast;
	}

	public long getCastStartTime() {
		return castStartTime;
	}

	public float[] getChargeTimes() {
		return chargeTimes;
	}

	/**
	 * @return the instantSkill
	 */
	public boolean isInstantSkill() {
		return instantSkill;
	}

	public int getHate() {
		return hate;
	}

	public void setHate(int hate) {
		this.hate = hate;
	}

	private boolean isMagical() {
		return skillTemplate.getType() == SkillType.MAGICAL && skillTemplate.getSubType() != SkillSubType.NONE;
	}
}
