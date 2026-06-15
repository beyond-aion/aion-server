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
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.controllers.observer.DeathObserver;
import com.aionemu.gameserver.controllers.observer.StartMovingListener;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.MotionData.AnimationTimes;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CASTSPELL;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CASTSPELL_RESULT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
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
	private int clientHitTime; // from CM_CASTSPELL
	private int hitTime; // time when effect is applied
	private float castSpeedForAnimationBoostAndChargeSkills; // cast speed can boost the animation time of the current skill and the hit time of the following skill
	private long castStartTime;
	private String chainCategory = null;
	private int chainUsageDuration = 0;
	private int hate;
	private volatile DeathObserver firstTargetDieObserver;

	public enum SkillMethod {
		CAST,
		ITEM,
		PASSIVE,
		PROVOKED,
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
			if (skillMethod == SkillMethod.CAST && chainCategory == null) // category gets set in preCastCheck()
				player.getChainSkills().resetChain();

			if (skillTemplate.getCounterSkill() != null) {
				long time = player.getLastCounterSkill(skillTemplate.getCounterSkill());
				if ((time + 5000) < System.currentTimeMillis()) {
					log.debug("chain skill failed, too late");
					return false;
				}
			}

			if (skillMethod == SkillMethod.ITEM && baseCastDuration > 0 && player.getMoveController().isInMove()) {
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
		if (player.isUsingFlightTransporterOrWindstream())
			return false;
		if (!getSkillTemplate().hasEvadeEffect() && player.getEffectController().isInAnyAbnormalState(AbnormalState.CANT_ATTACK_STATE))
			return false;
		if (player.getStore() != null)
			return false;
		return true;
	}

	private boolean isValidTarget(Player player, Creature target) {
		if (target instanceof Player targetPlayer) {
			if (targetPlayer.isUsingFlightTransporterOrWindstream())
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
		return useSkill(SecurityConfig.CHECK_ANIMATIONS, true);
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

		updateCastDurationAndSpeed();
		updateHitTime(checkAnimation);

		// notify skill use observers
		if (skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.ITEM)
			effector.getObserveController().notifyStartSkillCastObservers(this);

		// start casting
		effector.setCasting(this);

		// send packets to start casting
		if (skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.ITEM) {
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
		if (skillTemplate.isCharge()) {
			ThreadPoolManager.getInstance().schedule(this::cancelCurrentSkillCast, castDuration);
		} else if (castDuration > 0) {
			ThreadPoolManager.getInstance().schedule(this::endCast, castDuration);
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
		}
	}

	public int getCooldown() {
		int cooldown = effector.getSkillCooldown(skillTemplate);
		if (cooldown != 0)
			if (skillTemplate.getCooldownDeltaLv() != 0)
				cooldown = cooldown + skillTemplate.getCooldownDeltaLv() * skillLevel;
		return cooldown;
	}

	protected void updateCastDurationAndSpeed() {
		if (effector instanceof Npc npc) { // TODO: check if all skills should be effected
			castDuration = Math.round(baseCastDuration * (npc.getGameStats().getCastSpeed() / 1000f));
			castSpeedForAnimationBoostAndChargeSkills = 1f;
		} else if (skillTemplate.isCharge()) {
			boolean isChargeTimeFixed = updateChargeBaseCastDuration();
			castDuration = isChargeTimeFixed ? baseCastDuration : calculateChargeCastDuration();
			castSpeedForAnimationBoostAndChargeSkills = (float) castDuration / baseCastDuration;
		} else {
			castDuration = calculateCastDuration();
			castSpeedForAnimationBoostAndChargeSkills = 1 - effector.getGameStats().getStat(StatEnum.BOOST_CASTING_TIME, 1000).getBonus() / 1000f;
		}
	}

	private boolean updateChargeBaseCastDuration() {
		// cast/attack speed can affect charge time since 4.8 (https://aionpowerbook.com/powerbook/New_World_Update_-_Skill_Changes#Other_Changes)
		boolean isChargeTimeFixed = !isCastDurationAffectedByCastSpeed(); // fear and sleep charge skills are excluded, just like with regular casts
		SkillChargeCondition chargeCondition = skillTemplate.getSkillChargeCondition();
		if (chargeCondition != null) {
			int maxCastDuration = 0;
			ChargeSkillEntry skillCharge = DataManager.SKILL_CHARGE_DATA.getChargedSkillEntry(chargeCondition.getValue());
			for (ChargedSkill chargedSkill : skillCharge.getSkills()) {
				if (!isChargeTimeFixed && !DataManager.SKILL_DATA.getSkillTemplate(chargedSkill.getId()).isCastDurationAffectedByCastSpeed())
					isChargeTimeFixed = true;
				maxCastDuration += chargedSkill.getTime();
			}
			baseCastDuration = maxCastDuration;
		}
		return isChargeTimeFixed;
	}

	private int calculateChargeCastDuration() {
		boolean isPhysicalClass = effector instanceof Player player
			&& (player.getPlayerClass().isPhysicalClass() || player.getPlayerClass() == PlayerClass.RIDER || player.getPlayerClass() == PlayerClass.GUNNER);
		int castDuration;
		if (isPhysicalClass) // TODO check if attack speed should also affect magical classes
			castDuration = (int) effector.getGameStats().getPositiveStat(StatEnum.ATTACK_SPEED, baseCastDuration);
		else
			castDuration = calculateMagicalCastDuration();
		return Math.max(castDuration, (int) (baseCastDuration * 0.25f));
	}

	private int calculateCastDuration() {
		// ap & cash revival stones, or 2nd+ time of multicast-skill activation
		if (getSkillId() == 10802 || getMultiCastCount() > 0)
			return 0;
		if (skillTemplate.getType() != SkillType.MAGICAL || !isCastDurationAffectedByCastSpeed())
			return baseCastDuration;
		return calculateMagicalCastDuration();
	}

	private int calculateMagicalCastDuration() {
		int baseDurationCap = Math.round(baseCastDuration * 0.25f);
		//casting time stats cap 75%
		int castDuration = Math.max(effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME, baseCastDuration), baseDurationCap);
		int boostValue = effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME_SKILL, baseCastDuration);
		StatEnum skillCastBoostStat = getSkillCastBoostStat();
		if (skillCastBoostStat != null)
			boostValue = effector.getGameStats().getPositiveReverseStat(skillCastBoostStat, boostValue);

		int buffDelta = baseCastDuration - boostValue;
		castDuration -= buffDelta;
		
		if (!isSummonType(skillTemplate.getSubType())) {
			castDuration = Math.max(castDuration, baseDurationCap);
		}
		return Math.max(castDuration, 0);
	}

	private StatEnum getSkillCastBoostStat() {
		return switch (skillTemplate.getSubType()) {
			case SUMMON -> StatEnum.BOOST_CASTING_TIME_SUMMON;
			case SUMMONHOMING -> StatEnum.BOOST_CASTING_TIME_SUMMONHOMING;
			case SUMMONTRAP -> StatEnum.BOOST_CASTING_TIME_TRAP;
			case HEAL -> StatEnum.BOOST_CASTING_TIME_HEAL;
			case ATTACK -> StatEnum.BOOST_CASTING_TIME_ATTACK;
			default -> null;
		};
	}

	private boolean isSummonType(SkillSubType type) {
		return type == SkillSubType.SUMMON || type == SkillSubType.SUMMONHOMING || type == SkillSubType.SUMMONTRAP;
	}

	protected void updateHitTime(boolean checkAnimation) {
		hitTime = clientHitTime;
		if (!checkAnimation || !(effector instanceof Player player) || skillMethod != SkillMethod.CAST && skillMethod != SkillMethod.ITEM)
			return;

		float animationTimeUntilFirstHit = DataManager.MOTION_DATA.calculateAnimationTimeUntilFirstHit(player, this);
		int toleranceMillis = 1;
		if (skillTemplate.getAmmoSpeed() != 0) {
			float distance = (float) PositionUtil.getDistance(player, firstTarget);
			if (player.getMoveController().isInMove() || firstTarget.getMoveController().isInMove()) // subtract the run distance until ammo is actually fired
				distance -= PositionUtil.calculateMaxCoveredDistance(player, Math.round(animationTimeUntilFirstHit));
			float distanceTolerance = getDistanceTolerance(player, firstTarget);
			float ammoTime = Math.max(0, distance / skillTemplate.getAmmoSpeed() * 1000);
			toleranceMillis += Math.max(0, (int) Math.ceil(distanceTolerance / skillTemplate.getAmmoSpeed() * 1000));
			animationTimeUntilFirstHit += ammoTime;
		}

		int motionDelay = skillTemplate.getMotion() == null ? 0 : skillTemplate.getMotion().getDelay();
		int serverHitTime = motionDelay + Math.round(animationTimeUntilFirstHit);
		if (serverHitTime > clientHitTime) {
			hitTime = serverHitTime;
			if (isSuspiciousClientHitTime(clientHitTime, serverHitTime, toleranceMillis, player)) {
				List<String> uncertainties = collectUncertaintyFactorsForHitTime(player, toleranceMillis);
				String uncertaintyFactors = uncertainties.isEmpty() ? "" : " Uncertainty factors: " + String.join(", ", uncertainties);
				AuditLogger.log(player,
					"modified hit time for skill %d (client < server: %d/%d).%s".formatted(getSkillId(), clientHitTime, serverHitTime, uncertaintyFactors));
			}
		}
	}

	private float getDistanceTolerance(Player player, Creature target) {
		long nowMillis = System.currentTimeMillis();
		// even when not yet moving on server side, the player can just have started to move before casting (CM_MOVE is sent after CM_CASTSPELL)
		long maxMovementMillis = player.getMoveController().isInMove() ? 1000 : 200;
		long movementMillis = Math.min(maxMovementMillis, nowMillis - player.getMoveController().getLastMoveUpdate());
		float distanceTolerance = PositionUtil.calculateMaxCoveredDistance(player, movementMillis);
		if (target.getMoveController().isInMove())
			distanceTolerance += PositionUtil.calculateMaxCoveredDistance(target, nowMillis - target.getMoveController().getLastMoveUpdate());
		return distanceTolerance;
	}

	private boolean isSuspiciousClientHitTime(int clientHitTime, int serverHitTime, int tolerance, Player player) {
		if (clientHitTime >= serverHitTime - tolerance)
			return false;
		if (clientHitTime == 0 && (itemTemplate != null || skillTemplate.getMotion() != null && skillTemplate.getMotion().isInstantSkill()))
			return false; // effects apply immediately (damage too, though visually delayed)
		if (clientHitTime == 0 && player.isInRobotMode() && (player.getLastSkill().isMultiCast() || DataManager.SKILL_CHARGE_DATA.isChargeSkill(player.getLastSkill())))
			return false; // AT sends no hitTime when casting a non-instant skill within the animation time of a previous multiCast or charge skill, like 2640
		return true;
	}

	private List<String> collectUncertaintyFactorsForHitTime(Player player, int toleranceMillis) {
		List<String> uncertainties = new ArrayList<>();
		if (allowAnimationBoostByCastSpeed() && !player.isHitTimeBoosted())
			uncertainties.add("cast speed");
		if (skillTemplate.getAmmoSpeed() != 0)
			uncertainties.add("movement (calculated tolerance: " + toleranceMillis + " ms)");
		if (clientHitTime == 0 && player.isInRobotMode()) // TODO remove once isSuspiciousClientHitTime() identifies all false positives 
			uncertainties.add("Aethertech being weird 🤷‍♂️ (previous skill: " + player.getLastSkill().getSkillId() + ")");
		return uncertainties;
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
		if (skillMethod == SkillMethod.CAST || needsCast) {
			switch (targetType) {
				case 0: // PlayerObjectId as Target
					PacketSendUtility.broadcastPacketAndReceive(effector,
						new SM_CASTSPELL(effector, skillTemplate.getSkillId(), skillLevel, targetType, targetObjId, castDuration,
							castSpeedForAnimationBoostAndChargeSkills, allowAnimationBoostByCastSpeed()));
					if (effector instanceof Npc) {
						ShoutEventHandler.onCast((NpcAI) effector.getAi(), firstTarget);
					}
					break;

				case 3: // Target not in sight?
					PacketSendUtility.broadcastPacketAndReceive(effector,
						new SM_CASTSPELL(effector, skillTemplate.getSkillId(), skillLevel, targetType, targetObjId, castDuration,
							castSpeedForAnimationBoostAndChargeSkills, allowAnimationBoostByCastSpeed()));
					break;

				case 1: // XYZ as Target
					PacketSendUtility.broadcastPacketAndReceive(effector,
						new SM_CASTSPELL(effector, skillTemplate.getSkillId(), skillLevel, targetType, x, y, z, castDuration,
							castSpeedForAnimationBoostAndChargeSkills, allowAnimationBoostByCastSpeed()));
					break;
			}
			if (effector instanceof Player player)
				player.setNextSkillUse(System.currentTimeMillis() + GSConfig.MIN_SKILL_CAST_INTERVAL_MILLIS);
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
			firstTargetDieObserver = new DeathObserver(_ -> getEffector().getController().cancelCurrentSkill(null, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_LOST()));
			firstTarget.getObserveController().attach(firstTargetDieObserver);
		}

	}

	public void cancelCast() {
		if (isCancelled)
			return;
		isCancelled = true;
		removeObservers();
	}

	private void cancelCurrentSkillCast() {
		if (!isCancelled && equals(effector.getCastingSkill()))
			effector.getController().cancelCurrentSkill(null, null);
	}

	/**
	 * Apply effects and perform actions specified in skill template
	 */
	protected void endCast() {
		removeObservers();
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
			if (getMultiCastCount() > 0)
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

		if (isInstantSkill())
			applyEffect(effects);
		else
			ThreadPoolManager.getInstance().schedule(() -> applyEffect(effects), hitTime);

		if (skillMethod == SkillMethod.PENALTY || skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.ITEM) {
			boolean sentCastSpellResultPacket = sendCastSpellEnd(dashStatus, effects);
			if (sentCastSpellResultPacket && skillMethod != SkillMethod.PENALTY && effector instanceof Player player) {
				// animation times must be calculated after applyEffect of instant skills in order to honor speed buffs from this skill
				AnimationTimes animation = DataManager.MOTION_DATA.calculateAnimationTimesAfterLastHit(player, this);
				long nowMillis = System.currentTimeMillis();
				if (animation != null && allowAnimationBoostByCastSpeed()) {
					int latencyToleranceMillis = 50; // animation starts after client receives SM_CASTSPELL_RESULT, so add a few milliseconds
					player.setHitTimeBoost(nowMillis + animation.fullDurationMillis() + latencyToleranceMillis, getCastSpeedForAnimationBoostAndChargeSkills());
				} else {
					player.setHitTimeBoost(0, 0);
				}
				if (animation != null) // Math.max because nextSkillUse set from startCast() must not be undercut
					player.setNextSkillUse(Math.max(player.getNextSkillUse(), nowMillis + animation.lastHitMillis()));
			}
		}

		if (skillTemplate.isDeityAvatar() && effector instanceof Player player) {
			AbyssService.announceAbyssSkillUsage(player, skillTemplate.getL10n());
		}

		effector.getAi().onEndUseSkill(skillTemplate, skillLevel);
		if (effector instanceof Npc npc) {
			NpcSkillEntry lastSkill = npc.getGameStats().getLastSkill();
			if (lastSkill != null)
				lastSkill.fireOnEndCastEvents(npc);

			SkillAttackManager.afterUseSkill((NpcAI) npc.getAi());
		}

		if (skillMethod == SkillMethod.CAST) {
			effector.getObserveController().notifyEndSkillCastObservers(this);
		}
		effector.getWorldMapInstance().getInstanceHandler().onEndCastSkill(this);
	}

	private void removeObservers() {
		if (firstTargetDieObserver != null)
			firstTarget.getObserveController().removeObserver(firstTargetDieObserver);
		effector.getObserveController().removeObserver(moveListener);
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

	private boolean sendCastSpellEnd(int dashStatus, List<Effect> effects) {
		boolean sentCastSpellPacket = false;
		if (itemTemplate != null && !itemTemplate.isCombatActivated()) {
			PacketSendUtility.broadcastPacketAndReceive(effector,
				new SM_ITEM_USAGE_ANIMATION(effector.getObjectId(), firstTarget.getObjectId(), itemObjectId, itemTemplate.getTemplateId(), 0, 1, 0));
		} else {
			AIEventType et = skillTemplate.getSubType() == SkillSubType.ATTACK ? AIEventType.CREATURE_NEEDS_HELP : null;
			switch (targetType) {
				case 0: // PlayerObjectId as Target
				case 3: // Target not in sight?
					PacketSendUtility.broadcastPacketAndReceive(effector, new SM_CASTSPELL_RESULT(this, effects, hitTime, chainSuccess, dashStatus), et);
					sentCastSpellPacket = true;
					break;
				case 1: // XYZ as Target
					PacketSendUtility.broadcastPacketAndReceive(effector,
						new SM_CASTSPELL_RESULT(this, effects, hitTime, chainSuccess, dashStatus, targetType), et);
					sentCastSpellPacket = true;
					break;
			}
		}
		if (skillMethod == SkillMethod.ITEM && effector instanceof Player player)
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_USE_ITEM(getItemTemplate().getL10n()));
		return sentCastSpellPacket;
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

	protected void setCastStartTime(long castStartTime) {
		this.castStartTime = castStartTime;
	}

	public void setClientHitTime(int time) {
		this.clientHitTime = time;
	}

	public int getHitTime() {
		return hitTime;
	}

	protected void setCastSpeedForAnimationBoostAndChargeSkills(float castSpeedForAnimationBoostAndChargeSkills) {
		this.castSpeedForAnimationBoostAndChargeSkills = castSpeedForAnimationBoostAndChargeSkills;
	}

	public float getCastSpeedForAnimationBoostAndChargeSkills() {
		return castSpeedForAnimationBoostAndChargeSkills;
	}

	/**
	 * The game client allows to boost the animation time of a skill via cast speed:<br>
	 * - only half of the {@link #castSpeedForAnimationBoostAndChargeSkills cast speed boost} is considered and only if this boost exceeds the attack speed boost
	 * - animation time considers the current cast speed boost<br>
	 * - hit time considers cast speed boost of the previously cast skill
	 * - hit time will only be boosted if the current skill if cast before the animation of the previous skill finishes<br>
	 */
	public boolean allowAnimationBoostByCastSpeed() {
		return isMagical();
	}

	private boolean isCastDurationAffectedByCastSpeed() {
		return skillMethod == SkillMethod.CAST && skillTemplate.isCastDurationAffectedByCastSpeed();
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

	public int getMultiCastCount() {
		return skillTemplate.isMultiCast() && effector instanceof Player p ? p.getChainSkills().getCurrentChainCount(chainCategory) : 0;
	}

	public long getCastStartTime() {
		return castStartTime;
	}

	public boolean isInstantSkill() {
		return hitTime == 0 || skillTemplate.getMotion() != null && skillTemplate.getMotion().isInstantSkill();
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
