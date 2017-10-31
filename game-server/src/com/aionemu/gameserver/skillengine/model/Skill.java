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
import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.controllers.observer.DieObserver;
import com.aionemu.gameserver.controllers.observer.StartMovingListener;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.skill.NpcSkillTemplateEntry;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CASTSPELL;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CASTSPELL_RESULT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUIT_RESPONSE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_CANCEL;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.restrictions.RestrictionsManager;
import com.aionemu.gameserver.services.MotionLoggingService;
import com.aionemu.gameserver.services.abyss.AbyssService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.action.Action;
import com.aionemu.gameserver.skillengine.action.Actions;
import com.aionemu.gameserver.skillengine.condition.Conditions;
import com.aionemu.gameserver.skillengine.condition.SkillChargeCondition;
import com.aionemu.gameserver.skillengine.properties.FirstTargetAttribute;
import com.aionemu.gameserver.skillengine.properties.Properties;
import com.aionemu.gameserver.skillengine.properties.Properties.CastState;
import com.aionemu.gameserver.skillengine.properties.TargetRangeAttribute;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer
 * @modified Wakizashi, Neon
 */
public class Skill {

	private static final Logger log = LoggerFactory.getLogger(Skill.class);

	private final List<Creature> effectedList;
	private Creature firstTarget;
	protected final Creature effector;
	private final int skillLevel;
	private final SkillMethod skillMethod;
	protected final StartMovingListener conditionChangeListener;
	private final DieObserver dieObserver;
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
	private FirstTargetAttribute firstTargetAttribute;
	private TargetRangeAttribute targetRangeAttribute;
	/**
	 * Duration that depends on BOOST_CASTING_TIME
	 */
	private int baseCastDuration;
	private int castDuration;
	private int hitTime;// from CM_CASTSPELL
	private int serverTime;// time when effect is applied
	private long castStartTime;
	private boolean instantSkill = false;
	private String chainCategory = null;
	private int chainUsageDuration = 0;
	private volatile boolean isMultiCast = false;
	private float[] chargeTimes;
	private final boolean isPenaltySkill;

	public enum SkillMethod {
		CAST,
		ITEM,
		PASSIVE,
		PROVOKED,
		CHARGE
	}

	/**
	 * Each skill is a separate object upon invocation Skill level will be populated from player SkillList
	 * 
	 * @param skillTemplate
	 * @param effector
	 * @param firstTarget
	 */
	public Skill(SkillTemplate skillTemplate, Player effector, Creature firstTarget) {
		this(skillTemplate, effector, effector.getSkillList().getSkillLevel(skillTemplate.getSkillId()), firstTarget, null, false);
	}

	public Skill(SkillTemplate skillTemplate, Player effector, Creature firstTarget, int skillLevel) {
		this(skillTemplate, effector, skillLevel, firstTarget, null, false);
	}

	/**
	 * @param skillTemplate
	 * @param effector
	 * @param skillLvl
	 * @param firstTarget
	 */
	public Skill(SkillTemplate skillTemplate, Creature effector, int skillLvl, Creature firstTarget, ItemTemplate itemTemplate,
		boolean isPenaltySkill) {
		this.effectedList = new ArrayList<>();
		this.conditionChangeListener = new StartMovingListener();
		this.dieObserver = new DieObserver(this);
		this.firstTarget = firstTarget;
		this.skillLevel = skillLvl;
		this.skillTemplate = skillTemplate;
		this.effector = effector;
		this.baseCastDuration = skillTemplate.getDuration();
		this.castDuration = skillTemplate.getDuration();
		this.itemTemplate = itemTemplate;
		this.isPenaltySkill = isPenaltySkill;

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
		if (effector instanceof Player) {
			Player player = (Player) effector;
			if ((skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.CHARGE) && chainCategory == null) // category gets set in preCastCheck()
				player.getChainSkills().resetChain();

			if (this.skillTemplate.getCounterSkill() != null) {
				long time = player.getLastCounterSkill(skillTemplate.getCounterSkill());
				if ((time + 5000) < System.currentTimeMillis()) {
					log.debug("chain skill failed, too late");
					return false;
				}
			}

			if (skillMethod == SkillMethod.ITEM && castDuration > 0 && player.getMoveController().isInMove()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED(new DescriptionId(getItemTemplate().getNameId())));
				return false;
			}
		}

		return validateEffectedList();
	}

	private boolean validateEffectedList() {
		if (effector instanceof Player) {
			Player player = (Player) effector;
			effectedList.removeIf(effected -> !RestrictionsManager.canAffectBySkill(player, effected, this));
		}

		if (targetType == 0 && effectedList.isEmpty()) { // target selected but no target will be hit
			if (targetRangeAttribute != TargetRangeAttribute.AREA) { // don't restrict AoE activation
				if (effector instanceof Player)
					PacketSendUtility.sendPacket((Player) effector, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID());
				return false;
			}
		}

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

		// log skill time if effector instance of player
		// TODO config
		if (effector instanceof Player)
			MotionLoggingService.getInstance().logTime((Player) effector, this.getSkillTemplate(), this.getHitTime(),
				PositionUtil.getDistance(effector, firstTarget));

		// send packets to start casting
		if (skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.ITEM || skillMethod == SkillMethod.CHARGE) {
			castStartTime = System.currentTimeMillis();
			if (!isPenaltySkill)
				startCast();
			if (effector instanceof Npc)
				((NpcAI) effector.getAi()).setSubStateIfNot(AISubState.CAST);
		}

		effector.getObserveController().attach(conditionChangeListener);

		if (effector instanceof Npc) {
			Npc npc = (Npc) effector;
			NpcSkillEntry lastSkill = npc.getGameStats().getLastSkill();
			if (lastSkill == null) { // just in case the effector was forced to use the skill
				lastSkill = new NpcSkillTemplateEntry(new QueuedNpcSkillTemplate(skillTemplate.getSkillId(), skillLevel, 100));
				npc.getGameStats().setLastSkill(lastSkill);
			}
			lastSkill.setLastTimeUsed();
			npc.getGameStats().setNextSkillDelay(lastSkill.getNextSkillTime());
			npc.getAi().onStartUseSkill(lastSkill);
		}
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
		if (skillTemplate.getType() == SkillType.MAGICAL || skillMethod == SkillMethod.CHARGE) {
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
		}

		// 70% of base skill castDuration cap
		// No cast speed cap for skill Summoning Alacrity I(skillId: 1778) and Nimble Fingers I(skillId: 2386)
		if (!noBaseDurationCap) {
			int baseDurationCap = Math.round(baseCastDuration * 0.3f);
			if (castDuration < baseDurationCap) {
				castDuration = baseDurationCap;
			}
		}

		if (castDuration < 0)
			castDuration = 0;
	}

	private boolean checkAnimationTime() {
		if (!(effector instanceof Player) || skillMethod != SkillMethod.CAST)// TODO item skills?
			return true;
		Player player = (Player) effector;

		// if player is without weapon, dont check animation time
		if (player.getEquipment().getMainHandWeaponType() == null)
			return true;

		/**
		 * exceptions for certain skills -herb and mana treatment -traps
		 */
		if (getSkillTemplate().getGroup() != null) {
			switch (getSkillTemplate().getGroup()) {
				case "MENDING_L": // Bandage Heal
				case "MENDING": // Herb Treatment
				case "AROMATHERAPY": // Mana Treatment
				case "PR_FOCUSCASTING": // Prayer of focus
				case "RI_SUMMONARMOR": // Embark skill
					return true;
			}
		}

		if (player.getTransformModel().isActive() && player.getTransformModel().getType() == TransformType.FORM1)
			return true;

		if (getSkillTemplate().getSubType() == SkillSubType.SUMMONTRAP)
			return true;

		Motion motion = getSkillTemplate().getMotion();
		if (motion == null)
			return true; // some skills, like Blind Side (3467) or scroll/food buffs have no motion

		if (motion.isInstantSkill()) {
			if (hitTime != 0) {
				log.warn("Instant skill with hitTime > 0 (modified client_skills?) skill id: " + getSkillId() + ", " + player);
				return false;
			}
			if (motion.getName() == null) // skills like Remove Shock (283) or Feint (912)
				return true; // in this case, no update for NextSkillUse time is needed, so return instantly
		} else {
			if (motion.getName() == null)
				return true; // can't check animations without motion name (warning is sent on server startup, see DataManager.SKILL_DATA.validateMotions())
			if (hitTime == 0) {
				AuditLogger.log(player, "modified non-instant skill to hit instantly (skill id: " + getSkillId() + ")");
				return false;
			}
		}

		MotionTime motionTime = DataManager.MOTION_DATA.getMotionTime(motion.getName());
		if (motionTime == null) // no warning here (already sent on server startup to avoid permanent spam, see DataManager.SKILL_DATA.validateMotions())
			return true;

		WeaponTypeWrapper weapons = new WeaponTypeWrapper(player.getEquipment().getMainHandWeaponType(), player.getEquipment().getOffHandWeaponType());
		float serverTime = motionTime.getTimeForWeapon(player.getRace(), player.getGender(), weapons);
		int clientTime = hitTime;

		if (serverTime == 0) {
			if (!motionTime.isAllZero()) // only warn if motionTime isn't "empty" (warning for allZero motionTimes is sent on server startup)
				log.warn("missing weapon time for motionName: " + motion.getName() + " weapons: " + weapons.toString() + " Race: " + player.getRace()
					+ " Gender: " + player.getGender() + " skillId: " + getSkillId());
			return true;
		}

		// adjust client time with ammotime
		long ammoTime = 0;
		if (getSkillTemplate().getAmmoSpeed() != 0) {
			double distance = PositionUtil.getDistance(effector, firstTarget);
			ammoTime = Math.round(distance / getSkillTemplate().getAmmoSpeed() * 1000);// checked with client
			clientTime -= ammoTime;
		}

		// adjust servertime with motion play speed
		if (motion.getSpeed() != 100) {
			serverTime /= 100f;
			serverTime *= motion.getSpeed();
		}

		Stat2 attackSpeed = player.getGameStats().getAttackSpeed();

		// adjust serverTime with attackSpeed
		if (attackSpeed.getBase() != attackSpeed.getCurrent())
			serverTime *= ((float) attackSpeed.getCurrent() / (float) attackSpeed.getBase());

		// tolerance
		if (castDuration == 0)
			serverTime *= 0.85f;
		else
			serverTime *= 0.5f;

		int finalTime = Math.round(serverTime);
		if (motion.isInstantSkill() && hitTime == 0) {
			this.serverTime = (int) ammoTime;
		} else {
			if (clientTime < finalTime) {
				// check for no animation Hacks
				AuditLogger.log(player,
					"modified skill time for client skill: " + getSkillId() + " (clientTime < finalTime: " + clientTime + "/" + finalTime + ")");
				if (SecurityConfig.NO_ANIMATION) {
					// check if values are too low and disable skill usage / kick player
					if (clientTime < 0 || (clientTime / serverTime) < SecurityConfig.NO_ANIMATION_VALUE) {
						if (SecurityConfig.NO_ANIMATION_KICK) {
							player.getClientConnection().close(new SM_QUIT_RESPONSE());
						}
						return false;
					}
				}
			}
			this.serverTime = hitTime;
		}
		player.setNextSkillUse(System.currentTimeMillis() + castDuration + finalTime);
		return true;
	}

	/**
	 * Penalty success skill
	 */
	private void startPenaltySkill() {
		int penaltySkill = skillTemplate.getPenaltySkillId();
		if (penaltySkill == 0)
			return;

		SkillTemplate penaltyTemplate = DataManager.SKILL_DATA.getSkillTemplate(penaltySkill);
		String stack = penaltyTemplate.getStack();
		if (stack != null && (stack.equals("BA_N_SONGOFWARMTH_ADDEFFECT") || stack.equals("BA_N_SONGOFWIND_ADDEFFECT"))) {
			SkillEngine.getInstance().applyEffectDirectly(penaltySkill, effector, effector, 0);
			if (effector instanceof Player) {
				int count = 1;
				if (((Player) effector).isInTeam()) {
					for (Player p : ((Player) effector).getCurrentTeam().getMembers()) {
						if (count >= penaltyTemplate.getProperties().getTargetMaxCount()) {
							break;
						}
						if (p == null || !p.isOnline() || effector.equals(p) || p.isDead()) {
							continue;
						}
						if (PositionUtil.isInRange(effector, p, penaltyTemplate.getProperties().getEffectiveRange())) {
							SkillEngine.getInstance().applyEffectDirectly(penaltySkill, effector, p, 0);
							count++;
						}
					}
				}
			}
		} else {
			SkillEngine.getInstance().applyEffectDirectly(penaltySkill, firstTarget, effector, 0);
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
						new SM_CASTSPELL(effector, skillTemplate.getSkillId(), skillLevel, targetType, targetObjId, castDuration, castSpeed));
					if (effector instanceof Npc) {
						ShoutEventHandler.onCast((NpcAI) effector.getAi(), firstTarget);
					}
					break;

				case 3: // Target not in sight?
					PacketSendUtility.broadcastPacketAndReceive(effector,
						new SM_CASTSPELL(effector, skillTemplate.getSkillId(), skillLevel, targetType, targetObjId, castDuration, castSpeed));
					break;

				case 1: // XYZ as Target
					PacketSendUtility.broadcastPacketAndReceive(effector,
						new SM_CASTSPELL(effector, skillTemplate.getSkillId(), skillLevel, targetType, x, y, z, castDuration, castSpeed));
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
			firstTarget.getObserveController().attach(dieObserver);
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
		if (firstTarget != null) {
			firstTarget.getObserveController().removeObserver(dieObserver);
		}
		if (!effector.isCasting() || isCancelled || skillTemplate == null)
			return;

		// Check if target is out of skill range
		Properties properties = skillTemplate.getProperties();
		if (properties != null && !properties.endCastValidate(this)) {
			effector.getController().cancelCurrentSkill(null);
			return;
		}

		if (!validateEffectedList()) {
			effector.getController().cancelCurrentSkill(null);
			return;
		}

		if (!preUsageCheck()) {
			return;
		}

		effector.setCasting(null);

		/**
		 * try removing item, if its not possible return to prevent exploits
		 */
		if (effector instanceof Player && skillMethod == SkillMethod.ITEM) {
			Item item = ((Player) effector).getInventory().getItemByObjId(this.itemObjectId);
			if (item == null)
				return;
			if (item.getActivationCount() > 1) {
				item.setActivationCount(item.getActivationCount() - 1);
			} else {
				if (!((Player) effector).getInventory().decreaseByObjectId(item.getObjectId(), 1, ItemUpdateType.DEC_ITEM_USE))
					return;
			}
		}

		/**
		 * set instantSkill, must be before calculate effect
		 */
		Motion motion = skillTemplate.getMotion();
		if (motion != null && motion.isInstantSkill() || hitTime == 0)
			instantSkill = true;
		else if (skillTemplate.getSkillCategory() == SkillCategory.HEAL && !skillTemplate.getStack().equals("BA_SONGOFBLESS"))
			instantSkill = true; // TODO remove this condition entirely? should heals really always hit instantly?

		/**
		 * Perform necessary actions (use mp,dp items etc)
		 */
		Actions skillActions = skillTemplate.getActions();
		if (skillActions != null) {
			for (Action action : skillActions.getActions()) {
				if (!action.act(this))
					return;
			}
		}

		/**
		 * Create effects and precalculate result
		 */
		int dashStatus = 0;
		int resistCount = 0;
		boolean blockedChain = false;
		boolean blockedStance = false;
		final List<Effect> effects = new ArrayList<>();
		if (skillTemplate.getEffects() != null) {
			boolean blockAOESpread = false;
			for (Creature effected : effectedList) {
				// Find logic. Skill Steam Rushe Twice dmg
				String skillGroup = getSkillTemplate().getGroup();
				int count = 1;
				if (skillGroup != null && skillGroup.equalsIgnoreCase("RI_CHARGEATTACK"))
					count = 2;
				for (int i = 0; i < count; i++) {
					Effect effect = new Effect(this, effected, 0);
					if (effected instanceof Player) {
						if (effect.getEffectResult() == EffectResult.CONFLICT)
							blockedStance = true;
					}
					// Force RESIST status if AOE spell spread must be blocked
					if (blockAOESpread)
						effect.setAttackStatus(AttackStatus.RESIST);
					effect.initialize();
					final int worldId = effector.getWorldId();
					final int instanceId = effector.getInstanceId();
					effect.setWorldPosition(worldId, instanceId, x, y, z);

					effects.add(effect);
					dashStatus = effect.getDashStatus().getId();
					// Block AOE propagation if firstTarget resists the spell
					if ((!blockAOESpread) && (effect.getAttackStatus() == AttackStatus.RESIST) && (isTargetAOE()))
						blockAOESpread = true;

					if (effect.getAttackStatus() == AttackStatus.RESIST || effect.getAttackStatus() == AttackStatus.DODGE) {
						resistCount++;
					}
					for (Effect eventEffect : effector.getEffectController().getAbnormalEffectsToShow()) {
						if (eventEffect.getDuration() >= 86400000 && eventEffect.getSkillId() == 322) {
							dashStatus = 0;
						}
					}
				}
			}

			if (resistCount == effectedList.size()) {
				blockedChain = true;
				blockedPenaltySkill = true;
			}

			// exception for point point skills(example Ice Sheet)
			if (effectedList.isEmpty() && this.isPointPointSkill()) {
				Effect effect = new Effect(this, null, 0);
				effect.initialize();
				effect.setWorldPosition(effector.getWorldId(), effector.getInstanceId(), x, y, z);
				effects.add(effect);
				// spellStatus = effect.getSpellStatus().getId();
			}
		}

		if (effector instanceof Player && skillMethod == SkillMethod.CAST) {
			Player playerEffector = (Player) effector;
			if (playerEffector.getController().isUnderStance()) {
				playerEffector.getController().stopStance();
			}
			if (skillTemplate.isStance() && !blockedStance) {
				playerEffector.getController().startStance(skillTemplate.getSkillId());
			}
		}

		boolean setCooldowns = true;
		if (effector instanceof Player) {
			if (isMulticast() && ((Player) effector).getChainSkills().getCurrentChainCount(chainCategory) > 0)
				setCooldowns = false;

			// Check Chain Skill Trigger Rate, only for chain skills and only for player
			if (chainCategory != null) {
				if (blockedChain)
					chainSuccess = false;
				else
					chainSuccess = Rnd.chance() < skillTemplate.getChainSkillProb() || CustomConfig.SKILL_CHAIN_DISABLE_TRIGGERRATE;

				if (chainSuccess)
					((Player) effector).getChainSkills().updateChain(chainCategory, chainUsageDuration);
				else
					((Player) effector).getChainSkills().resetChain();
			}

			QuestEngine.getInstance().onUseSkill(new QuestEnv(effector.getTarget(), (Player) effector, 0), skillTemplate.getSkillId());
		}

		if (setCooldowns && !isPenaltySkill)
			setCooldowns();

		if (instantSkill)
			applyEffect(effects);
		else
			ThreadPoolManager.getInstance().schedule(() -> applyEffect(effects), hitTime);

		if (!isPenaltySkill && (skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.ITEM || skillMethod == SkillMethod.CHARGE))
			sendCastspellEnd(dashStatus, effects);

		endCondCheck();

		if (getSkillTemplate().isDeityAvatar() && effector instanceof Player) {
			AbyssService.rankerSkillAnnounce((Player) effector, getSkillTemplate().getNameId());
		}

		if (effector instanceof Npc) {
			Npc npc = (Npc) effector;
			NpcSkillEntry lastSkill = npc.getGameStats().getLastSkill();
			if (lastSkill != null) {
				if (lastSkill.isQueued()) {
					npc.getQueuedSkills().poll();
				}
				if (!npc.isDead()) {
					lastSkill.fireOnEndCastEvents(npc);
					npc.getAi().onEndUseSkill(lastSkill);
				}
			}
			SkillAttackManager.afterUseSkill((NpcAI) effector.getAi());
		}

		if (skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.CHARGE) {
			if (effector instanceof Player)
				effector.getObserveController().notifyEndSkillCastObservers(this);
		}
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
		/**
		 * Apply effects to effected objects
		 */
		effects.forEach(Effect::applyEffect);

		addResistedEffectHateAndNotifyFriends(effects);

		/**
		 * Use penalty skill (now 100% success)
		 */
		if (!blockedPenaltySkill)
			startPenaltySkill();
	}

	/**
	 * @param dashStatus
	 * @param effects
	 */
	private void sendCastspellEnd(int dashStatus, List<Effect> effects) {
		boolean needsCast = itemTemplate != null && itemTemplate.isCombatActivated();
		AIEventType et = null;
		if (skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.CHARGE || needsCast) {
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
				PacketSendUtility.sendPacket((Player) effector, SM_SYSTEM_MESSAGE.STR_USE_ITEM(new DescriptionId(getItemTemplate().getNameId())));
			}
		} else if (skillMethod == SkillMethod.ITEM) {

			// TODO: Find out when SM_CASTSPELL_RESULT should be sent with dashStatus = 2, and no SM_ITEM_USAGE_ANIMATION
			PacketSendUtility.broadcastPacketAndReceive(effector, new SM_ITEM_USAGE_ANIMATION(effector.getObjectId(), firstTarget.getObjectId(),
				(this.itemObjectId == 0 ? 0 : this.itemObjectId), itemTemplate.getTemplateId(), 0, 1, 0));
			if (effector instanceof Player)
				PacketSendUtility.sendPacket((Player) effector, SM_SYSTEM_MESSAGE.STR_USE_ITEM(new DescriptionId(getItemTemplate().getNameId())));
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
				if (firstTarget != null)
					firstTarget.getObserveController().removeObserver(dieObserver);
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
	public StartMovingListener getConditionChangeListener() {
		return conditionChangeListener;
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

	/**
	 * @param firstTargetAttribute
	 *          the firstTargetAttribute to set
	 */
	public void setFirstTargetAttribute(FirstTargetAttribute firstTargetAttribute) {
		this.firstTargetAttribute = firstTargetAttribute;
	}

	/**
	 * @return true if the present skill is a non-targeted, non-point AOE skill
	 */
	public boolean isNonTargetAOE() {
		return (firstTargetAttribute == FirstTargetAttribute.ME && targetRangeAttribute == TargetRangeAttribute.AREA);
	}

	/**
	 * @return true if the present skill is a targeted AOE skill
	 */
	private boolean isTargetAOE() {
		return (firstTargetAttribute == FirstTargetAttribute.TARGET && targetRangeAttribute == TargetRangeAttribute.AREA);
	}

	/**
	 * @return true if the present skill is a self buff includes items (such as scroll buffs)
	 */
	public boolean isSelfBuff() {
		return (firstTargetAttribute == FirstTargetAttribute.ME && targetRangeAttribute == TargetRangeAttribute.ONLYONE
			&& skillTemplate.getSubType() == SkillSubType.BUFF && !skillTemplate.isDeityAvatar());
	}

	/**
	 * @return true if the present skill has self as first target
	 */
	public boolean isFirstTargetSelf() {
		return (firstTargetAttribute == FirstTargetAttribute.ME);
	}

	/**
	 * @return true if the present skill is a Point skill
	 */
	public boolean isPointSkill() {
		return (firstTargetAttribute == FirstTargetAttribute.POINT);
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

	/**
	 * @param targetRangeAttribute
	 *          the targetRangeAttribute to set
	 */
	public void setTargetRangeAttribute(TargetRangeAttribute targetRangeAttribute) {
		this.targetRangeAttribute = targetRangeAttribute;
	}

	/**
	 * @param targetType
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setTargetType(int targetType, float x, float y, float z) {
		this.targetType = targetType;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Calculated position after skill
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param h
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

	/**
	 * @param time
	 *          The time to set.
	 */
	public void setHitTime(int time) {
		this.hitTime = time;
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

	private boolean isGroundSkill() {
		return skillTemplate.isGroundSkill();
	}

	public boolean shouldAffectTarget(VisibleObject object) {
		// If creature is at least 2 meters above the terrain, ground skill cannot be applied
		if (GeoDataConfig.GEO_ENABLE) {
			if (isGroundSkill()) {
				float geoZ = GeoService.getInstance().getZ(object, object.getZ() + 2, object.getZ() - 100);
				if (!Float.isNaN(geoZ) && object.getZ() - geoZ > 2f)
					return false;
			}
			return GeoService.getInstance().canSee(getFirstTarget(), object);
		}
		return true;
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
}
