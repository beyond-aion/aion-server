package com.aionemu.gameserver.skillengine.model;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AISubState;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.ai2.handler.ShoutEventHandler;
import com.aionemu.gameserver.ai2.manager.SkillAttackManager;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.controllers.observer.StartMovingListener;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
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
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.properties.FirstTargetAttribute;
import com.aionemu.gameserver.skillengine.properties.Properties;
import com.aionemu.gameserver.skillengine.properties.Properties.CastState;
import com.aionemu.gameserver.skillengine.properties.TargetRangeAttribute;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.world.geo.GeoService;
import com.aionemu.gameserver.world.knownlist.Visitor;

import javolution.util.FastTable;

/**
 * @author ATracer Modified by Wakzashi
 */
public class Skill {

	protected SkillMethod skillMethod = SkillMethod.CAST;

	private List<Creature> effectedList;

	private Creature firstTarget;

	protected Creature effector;
	private int skillLevel;

	private int skillStackLvl;

	protected StartMovingListener conditionChangeListener;

	private SkillTemplate skillTemplate;

	private boolean firstTargetRangeCheck = true;

	private ItemTemplate itemTemplate;
	private int itemObjectId = 0;

	private int targetType;

	private boolean chainSuccess = true;

	private boolean isCancelled = false;
	private boolean blockedPenaltySkill = false;

	private float x;
	private float y;
	private float z;
	private byte h;

	protected int boostSkillCost;

	private FirstTargetAttribute firstTargetAttribute;
	private TargetRangeAttribute targetRangeAttribute;

	/**
	 * Duration that depends on BOOST_CASTING_TIME
	 */
	private int duration;
	private int hitTime;// from CM_CASTSPELL
	private int serverTime;// time when effect is applied
	private long castStartTime;
	private boolean instantSkill = false;

	private String chainCategory = null;
	private volatile boolean isMultiCast = false;
	private List<ChargedSkill> chargeSkillList = new FastTable<ChargedSkill>();

	public enum SkillMethod {
		CAST,
		ITEM,
		PASSIVE,
		PROVOKED,
		CHARGE;
	}

	private Logger log = LoggerFactory.getLogger(Skill.class);

	/**
	 * Each skill is a separate object upon invocation Skill level will be populated from player SkillList
	 * 
	 * @param skillTemplate
	 * @param effector
	 * @param world
	 */
	public Skill(SkillTemplate skillTemplate, Player effector, Creature firstTarget) {
		this(skillTemplate, effector, effector.getSkillList().getSkillLevel(skillTemplate.getSkillId()), firstTarget, null);
	}

	public Skill(SkillTemplate skillTemplate, Player effector, Creature firstTarget, int skillLevel) {
		this(skillTemplate, effector, skillLevel, firstTarget, null);
	}

	/**
	 * @param skillTemplate
	 * @param effector
	 * @param skillLvl
	 * @param firstTarget
	 */
	public Skill(SkillTemplate skillTemplate, Creature effector, int skillLvl, Creature firstTarget, ItemTemplate itemTemplate) {
		this.effectedList = new FastTable<Creature>();
		this.conditionChangeListener = new StartMovingListener();
		this.firstTarget = firstTarget;
		this.skillLevel = skillLvl;
		this.skillStackLvl = skillTemplate.getLvl();
		this.skillTemplate = skillTemplate;
		this.effector = effector;
		this.duration = skillTemplate.getDuration();
		this.itemTemplate = itemTemplate;

		if (itemTemplate != null)
			skillMethod = SkillMethod.ITEM;
		else if (skillTemplate.isPassive())
			skillMethod = SkillMethod.PASSIVE;
		else if (skillTemplate.isProvoked())
			skillMethod = SkillMethod.PROVOKED;
		else if (skillTemplate.isCharge())
			skillMethod = SkillMethod.CHARGE;
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
			if (this.skillTemplate.getCounterSkill() != null) {
				long time = player.getLastCounterSkill(skillTemplate.getCounterSkill());
				if ((time + 5000) < System.currentTimeMillis()) {
					log.debug("chain skill failed, too late");
					return false;
				}
			}

			if (skillMethod == SkillMethod.ITEM && duration > 0 && player.getMoveController().isInMove()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED(new DescriptionId(getItemTemplate().getNameId())));
				return false;
			}
		}

		return validateEffectedList();
	}

	protected boolean validateEffectedList() {
		Iterator<Creature> effectedIter = effectedList.iterator();
		while (effectedIter.hasNext()) {
			Creature effected = effectedIter.next();
			if (effected == null)
				effected = effector;

			if (effector instanceof Player) {
				if (!RestrictionsManager.canAffectBySkill((Player) effector, effected, this))
					effectedIter.remove();
			} else {
				if (effector.getEffectController().isInAnyAbnormalState(AbnormalState.CANT_ATTACK_STATE))
					effectedIter.remove();
			}
		}

		// TODO: Enable non-targeted, non-point AOE skills to trigger.
		if (targetType == 0 && effectedList.isEmpty() && firstTargetAttribute != FirstTargetAttribute.ME
			&& targetRangeAttribute != TargetRangeAttribute.AREA) {
			log.debug("targettype failed");
			if (effector instanceof Player) {
				PacketSendUtility.sendPacket((Player) effector, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID);
			}
			return false;
		}

		return true;
	}

	/**
	 * Skill entry point
	 * 
	 * @return true if usage is successfull
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

		if (skillMethod != SkillMethod.CHARGE)
			calculateSkillDuration();

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
				MathUtil.getDistance(effector, firstTarget));

		// send packets to start casting
		if (skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.ITEM || skillMethod == SkillMethod.CHARGE) {
			castStartTime = System.currentTimeMillis();
			startCast();
			if (effector instanceof Npc)
				((NpcAI2) ((Npc) effector).getAi2()).setSubStateIfNot(AISubState.CAST);
		}

		effector.getObserveController().attach(conditionChangeListener);

		if (this.duration > 0) {
			schedule(this.duration);
		} else {
			endCast();
		}
		return true;
	}

	private void setCooldowns() {
		int cooldown = effector.getSkillCooldown(skillTemplate);
		if (cooldown != 0) {
			effector.setSkillCoolDown(skillTemplate.getCooldownId(), cooldown * 100 + System.currentTimeMillis());
			effector.setSkillCoolDownBase(skillTemplate.getCooldownId(), System.currentTimeMillis());
		}
	}

	protected void calculateSkillDuration() {
		// Skills that are not affected by boost casting time
		duration = 0;
		int boostValue = 0;

		if (isCastTimeFixed()) {
			duration = skillTemplate.getDuration();
			return;
		}
		boolean noBaseDurationCap = false;
		if (skillTemplate.getType() == SkillType.MAGICAL) {
			duration = effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME, skillTemplate.getDuration());
			boostValue = effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME_SKILL, skillTemplate.getDuration());
			switch (skillTemplate.getSubType()) {
				case SUMMON:
					boostValue = effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME_SUMMON, boostValue);
					if (effector.getEffectController().hasAbnormalEffect(1778))
						noBaseDurationCap = true;
					break;
				case SUMMONHOMING:
					boostValue = effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME_SUMMONHOMING, boostValue);
					if (effector.getEffectController().hasAbnormalEffect(1778))
						noBaseDurationCap = true;
					break;
				case SUMMONTRAP:
					int tempBoostVal = boostValue;
					boostValue = effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME_TRAP, boostValue);
					if (boostValue == 0 && duration < tempBoostVal) {
						boostValue = tempBoostVal - duration;
					}
					if (effector.getEffectController().hasAbnormalEffect(2386))
						noBaseDurationCap = true;
					break;
				case HEAL:
					boostValue = effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME_HEAL, boostValue);
					break;
				case ATTACK:
					boostValue = effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME_ATTACK, boostValue);
					break;
			}
			duration -= skillTemplate.getDuration() - boostValue;
		} else
			duration = skillTemplate.getDuration();

		// 70% of base skill duration cap
		// No cast speed cap for skill Summoning Alacrity I(skillId: 1778) and Nimble Fingers I(skillId: 2386)
		if (!noBaseDurationCap) {
			int baseDurationCap = Math.round(skillTemplate.getDuration() * 0.3f);
			if (duration < baseDurationCap) {
				duration = baseDurationCap;
			}
		}

		if (effector instanceof Player) {
			if (this.isMulticast()
				&& ((Player) effector).getChainSkills().getChainCount((Player) effector, this.getSkillTemplate(), this.chainCategory) != 0) {
				duration = 0;
			}
		}

		if (duration < 0) {
			duration = 0;
		}
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
		// dont check herb , mana treatment and concentration enhancement
		switch (this.getSkillId()) {
			case 1803:// bandage heal
			case 1804:// herb treatment
			case 1805:
			case 1825:
			case 1827:
			case 2672:
			case 1823:// mana treatment
			case 1824:
			case 1826:
			case 1828:
			case 2673:
			case 1078: // concentration enhancement
			case 1125:
			case 1468:
			case 11580:
			case 3597: // Embark skill
			case 3598:
			case 3599:
			case 3600:
			case 3601:
				return true;
		}

		if (player.getTransformModel().isActive()) {
			if (player.getTransformModel().getType() == TransformType.FORM1) {
				return true;
			}
		}

		if (this.getSkillTemplate().getSubType() == SkillSubType.SUMMONTRAP)
			return true;

		Motion motion = this.getSkillTemplate().getMotion();

		if (motion == null || motion.getName() == null) {
			log.warn("missing motion for skillId: " + this.getSkillId());
			return true;
		}

		if (motion.getInstantSkill() && hitTime != 0) {
			log.warn("Instant and hitTime not 0! modified client_skills? player objectid: " + player.getObjectId());
			return false;
		} else if (!motion.getInstantSkill() && hitTime == 0) {
			log.warn("modified client_skills! player objectid: " + player.getObjectId());
			return false;
		}

		MotionTime motionTime = DataManager.MOTION_DATA.getMotionTime(motion.getName());

		if (motionTime == null) {
			log.warn("missing motiontime for motionName: " + motion.getName() + " skillId: " + this.getSkillId());
			return true;
		}

		WeaponTypeWrapper weapons = new WeaponTypeWrapper(player.getEquipment().getMainHandWeaponType(), player.getEquipment().getOffHandWeaponType());
		float serverTime = motionTime.getTimeForWeapon(player.getRace(), player.getGender(), weapons);
		int clientTime = hitTime;

		if (serverTime == 0) {
			log.warn("missing weapon time for motionName: " + motion.getName() + " weapons: " + weapons.toString() + " Race: " + player.getRace()
				+ " Gender: " + player.getGender() + " skillId: " + this.getSkillId());
			return true;
		}

		// adjust client time with ammotime
		long ammoTime = 0;
		double distance = MathUtil.getDistance(effector, firstTarget);
		if (getSkillTemplate().getAmmoSpeed() != 0) {
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
		if (duration == 0)
			serverTime *= 0.85f;
		else
			serverTime *= 0.5f;

		int finalTime = Math.round(serverTime);
		if (motion.getInstantSkill() && hitTime == 0) {
			this.serverTime = (int) ammoTime;
		} else {
			if (clientTime < finalTime) {
				// check for no animation Hacks
				AuditLogger.info(player, "Modified skill time for client skill: " + getSkillId() + "\t(clientTime < finalTime: " + clientTime + "/" + finalTime + ")");
				if (SecurityConfig.NO_ANIMATION) {
					// check if values are too low and disable skill usage / kick player
					if (clientTime < 0 || (clientTime / serverTime) < SecurityConfig.NO_ANIMATION_VALUE) {
						if (SecurityConfig.NO_ANIMATION_KICK) {
							player.getClientConnection().close(new SM_QUIT_RESPONSE());
							AuditLogger.info(player, "Kicking player for No Ani: " + player.getName());
						}
						return false;
					}
				}
			}
			this.serverTime = hitTime;
		}
		player.setNextSkillUse(System.currentTimeMillis() + duration + finalTime);
		return true;
	}

	/**
	 * Penalty success skill
	 */
	private void startPenaltySkill() {
		int penaltySkill = skillTemplate.getPenaltySkillId();
		if (penaltySkill == 0)
			return;

		SkillEngine.getInstance().applyEffectDirectly(penaltySkill, firstTarget, effector, 0);
	}

	/**
	 * Start casting of skill
	 */
	protected void startCast() {
		int targetObjId = firstTarget != null ? firstTarget.getObjectId() : 0;
		boolean needsCast = itemTemplate != null && itemTemplate.isCombatActivated();
		float castSpeed = skillTemplate.getDuration() != 0 ? (float) duration / skillTemplate.getDuration() : effector.getGameStats()
			.getReverseStat(StatEnum.BOOST_CASTING_TIME, 1000).getCurrent() / 1000f;
		if (skillMethod == SkillMethod.CHARGE)
			castSpeed = 1.0f;
		if (skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.CHARGE || needsCast) {
			switch (targetType) {
				case 0: // PlayerObjectId as Target
					PacketSendUtility.broadcastPacketAndReceive(effector, new SM_CASTSPELL(effector, skillTemplate.getSkillId(), skillLevel, targetType,
						targetObjId, this.duration, castSpeed));
					if (effector instanceof Npc && firstTarget instanceof Player) {
						NpcAI2 ai = (NpcAI2) effector.getAi2();
						if (ai.poll(AIQuestion.CAN_SHOUT))
							ShoutEventHandler.onCast(ai, firstTarget);
					}
					break;

				case 3: // Target not in sight?
					PacketSendUtility.broadcastPacketAndReceive(effector, new SM_CASTSPELL(effector, skillTemplate.getSkillId(), skillLevel, targetType,
						targetObjId, this.duration, castSpeed));
					break;

				case 1: // XYZ as Target
					PacketSendUtility.broadcastPacketAndReceive(effector, new SM_CASTSPELL(effector, skillTemplate.getSkillId(), skillLevel, targetType, x, y,
						z, this.duration, castSpeed));
					break;
			}
		} else if (skillMethod == SkillMethod.ITEM && duration > 0) {
			PacketSendUtility.broadcastPacketAndReceive(effector, new SM_ITEM_USAGE_ANIMATION(effector.getObjectId(), firstTarget.getObjectId(),
				(this.itemObjectId == 0 ? 0 : this.itemObjectId), itemTemplate.getTemplateId(), this.duration, 0, 0));
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
		if (!effector.isCasting() || isCancelled)
			return;

		// if target out of range
		if (skillTemplate == null)
			return;

		// Check if target is out of skill range
		Properties properties = skillTemplate.getProperties();
		if (properties != null && !properties.endCastValidate(this)) {
			effector.getController().cancelCurrentSkill();
			return;
		}

		if (!validateEffectedList()) {
			effector.getController().cancelCurrentSkill();
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
		Motion motion = this.getSkillTemplate().getMotion();
		if (skillTemplate.getSkillCategory() == SkillCategory.HEAL)
			instantSkill = true;
		else if (motion != null && motion.getInstantSkill())
			instantSkill = true;
		else if (hitTime == 0)
			instantSkill = true;

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
		final List<Effect> effects = new FastTable<Effect>();
		if (skillTemplate.getEffects() != null) {
			boolean blockAOESpread = false;
			for (Creature effected : effectedList) {
				// Find logic. Skill Steam Rushe Twice dmg
				String skillGroup = getSkillTemplate().getGroup();
				int count = 1;
				if (skillGroup != null && skillGroup.equalsIgnoreCase("RI_CHARGEATTACK"))
					count = 2;
				for (int i = 0; i < count; i++) {
					Effect effect = new Effect(this, effected, 0, itemTemplate);
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
				Effect effect = new Effect(this, null, 0, itemTemplate);
				effect.initialize();
				final int worldId = effector.getWorldId();
				final int instanceId = effector.getInstanceId();
				effect.setWorldPosition(worldId, instanceId, x, y, z);
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
			if (this.isMulticast()
				&& ((Player) effector).getChainSkills().getChainCount((Player) effector, this.getSkillTemplate(), this.chainCategory) != 0) {
				setCooldowns = false;
			}
		}

		// Check Chain Skill Trigger Rate, only for chain skills and only for player
		if (effector instanceof Player && this.chainCategory != null) {
			if (blockedChain)
				this.chainSuccess = false;
			else {
				int chainProb = skillTemplate.getChainSkillProb();
				if (!CustomConfig.SKILL_CHAIN_TRIGGERRATE)
					chainProb = 100;

				if (chainProb < 100)
					this.chainSuccess = Rnd.get(90) < chainProb;
			}
		}

		/**
		 * set variables for chaincondition check
		 */
		if (effector instanceof Player && this.chainSuccess && this.chainCategory != null) {
			((Player) effector).getChainSkills().addChainSkill(this.chainCategory, this.isMulticast());
		}

		if (effector instanceof Player) {
			QuestEnv env = new QuestEnv(effector.getTarget(), (Player) effector, 0, 0);
			QuestEngine.getInstance().onUseSkill(env, skillTemplate.getSkillId());
		}

		if (setCooldowns)
			this.setCooldowns();

		if (instantSkill)
			applyEffect(effects);
		else {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					applyEffect(effects);
				}
			}, hitTime);
		}
		if (skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.ITEM || skillMethod == SkillMethod.CHARGE)
			sendCastspellEnd(dashStatus, effects);

		endCondCheck();
		addHateAndNotifyFriends(effects);
		
		if (this.getSkillTemplate().isDeityAvatar() && effector instanceof Player) {
			AbyssService.rankerSkillAnnounce((Player) effector, this.getSkillTemplate().getNameId());
		}

		if (effector instanceof Npc) {
			Npc npc = (Npc) effector;
			if (npc.getGameStats().getLastSkill() != null) {
				npc.getGameStats().getLastSkill().fireAfterUseSkillEvents(npc);
			}
			SkillAttackManager.afterUseSkill((NpcAI2) ((Npc) effector).getAi2());
		}

		if (skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.CHARGE) {
			if (effector instanceof Player)
				effector.getObserveController().notifyEndSkillCastObservers(this);
		}
	}

	private void addHateAndNotifyFriends(List<Effect> effects) {
		if (effects == null || effects.isEmpty()) {
			return;
		}
		for (Effect effect : effects) {
			if (effect.getTauntHate() >= 0) {
				//delayed skills do not add hate if they were not resisted/dodged
				if (effect.isDelayedDamage() && (effect.getAttackStatus() == AttackStatus.RESIST || effect.getAttackStatus() == AttackStatus.DODGE)) {
					continue;
				}
				effect.getEffected().getAggroList().addHate(effector, 1);
				effect.getEffected().getKnownList().doOnAllNpcs(new Visitor<Npc>() {

					@Override
					public void visit(Npc object) {
						object.getAi2().onCreatureEvent(AIEventType.CREATURE_NEEDS_SUPPORT, effect.getEffected());
					}

				});
			}
		}
	}

	public void applyEffect(List<Effect> effects) {
		/**
		 * Apply effects to effected objects
		 */
		for (Effect effect : effects) {
			effect.applyEffect();
		}

		/**
		 * Use penalty skill (now 100% success)
		 */
		if (!blockedPenaltySkill)
			startPenaltySkill();
	}

	/**
	 * @param spellStatus
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
					PacketSendUtility.broadcastPacketAndReceive(effector, new SM_CASTSPELL_RESULT(this, effects, serverTime, chainSuccess, dashStatus), et);
					break;

				case 3: // Target not in sight?
					PacketSendUtility.broadcastPacketAndReceive(effector, new SM_CASTSPELL_RESULT(this, effects, serverTime, chainSuccess, dashStatus), et);
					break;

				case 1: // XYZ as Target
					PacketSendUtility.broadcastPacketAndReceive(effector, new SM_CASTSPELL_RESULT(this, effects, serverTime, chainSuccess, dashStatus,
						targetType), et);
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
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isCancelled && skillMethod == SkillMethod.CHARGE) {
					cancelCast();
					effector.setCasting(null);
					PacketSendUtility.broadcastPacketAndReceive(effector, new SM_SKILL_CANCEL(effector, skillTemplate.getSkillId()));
					return;
				}
				endCast();
			}
		}, delay);
	}

	/**
	 * Check all conditions before starting cast
	 */
	protected boolean preCastCheck() {
		Conditions skillConditions = skillTemplate.getStartconditions();
		return skillConditions != null ? skillConditions.validate(this) : true;
	}

	/**
	 * Check all conditions before using skill
	 */
	private boolean preUsageCheck() {
		Conditions skillConditions = skillTemplate.getUseconditions();
		return skillConditions != null ? skillConditions.validate(this) : true;
	}

	/**
	 * Check all conditions after using skill
	 */
	private boolean endCondCheck() {
		Conditions skillConditions = skillTemplate.getEndConditions();
		return skillConditions != null ? skillConditions.validate(this) : true;
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
	 * @return the skillStackLvl
	 */
	public int getSkillStackLvl() {
		return skillStackLvl;
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
	 * @param FirstTargetAttribute
	 *          the firstTargetAttribute to set
	 */
	public void setFirstTargetAttribute(FirstTargetAttribute firstTargetAttribute) {
		this.firstTargetAttribute = firstTargetAttribute;
	}

	/**
	 * @return true if the present skill is a non-targeted, non-point AOE skill
	 */
	public boolean checkNonTargetAOE() {
		return (firstTargetAttribute == FirstTargetAttribute.ME && targetRangeAttribute == TargetRangeAttribute.AREA);
	}

	/**
	 * @return true if the present skill is a targeted AOE skill
	 */
	public boolean isTargetAOE() {
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
		return (this.firstTargetAttribute == FirstTargetAttribute.POINT);
	}

	/**
	 * @param firstTargetRangeCheck
	 *          the firstTargetRangeCheck to set
	 */
	public void setFirstTargetRangeCheck(boolean firstTargetRangeCheck) {
		this.firstTargetRangeCheck = firstTargetRangeCheck;
	}

	/**
	 * @param itemTemplate
	 *          the itemTemplate to set
	 */
	public void setItemTemplate(ItemTemplate itemTemplate) {
		this.itemTemplate = itemTemplate;
	}

	public ItemTemplate getItemTemplate() {
		return this.itemTemplate;
	}

	public void setItemObjectId(int id) {
		this.itemObjectId = id;
	}

	public int getItemObjectId() {
		return this.itemObjectId;
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

	public void setDuration(int t) {
		this.duration = t;
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
		if (skillMethod != SkillMethod.CAST) // only casted skills are affected
			return true;

		switch (this.getSkillId()) {
			case 1636:// Fear I
			case 2318:
			case 2319:
			case 1685:// Fear Shriek I
			case 2006:// Hand of Torpor
			case 2011:// Spirit Hypnosis
			case 1495:// Sleep I
			case 2316:
			case 2317:
			case 1443:// Sleeping Storm
			case 1454:// Curse of Roots
			case 1430:// Curse of Old Roots
			case 1497:// Tranquilizing Cloud I
			case 11885:// abyss transformation elyos
			case 11886:
			case 11887:
			case 11888:
			case 11889:
			case 11890:// abyss transformation asmo
			case 11891:
			case 11892:
			case 11893:
			case 11894:
			case 1801:// return skill
			case 1803:// herb treatment
			case 1804:
			case 1805:
			case 1825:
			case 1827:
			case 2672:
			case 1823:// mana treatment
			case 1824:
			case 1826:
			case 1828:
			case 2673:
			case 3512: // escape
				return true;
		}

		return false;
	}

	public boolean isGroundSkill() {
		return skillTemplate.isGroundSkill();
	}

	public boolean shouldAffectTarget(VisibleObject object) {
		// If creature is at least 2 meters above the terrain, ground skill cannot be applied
		if (GeoDataConfig.GEO_ENABLE) {
			if (isGroundSkill()) {
				if ((object.getZ() - GeoService.getInstance().getZ(object) > 1.0f) || (object.getZ() - GeoService.getInstance().getZ(object) < -2.0f))
					return false;
			}
			return GeoService.getInstance().canSee(getFirstTarget(), object);
		}
		return true;
	}

	public void setChainCategory(String chainCategory) {
		this.chainCategory = chainCategory;
	}

	public SkillMethod getSkillMethod() {
		return this.skillMethod;
	}

	public boolean isPointPointSkill() {
		if (this.getSkillTemplate().getProperties().getFirstTarget() == FirstTargetAttribute.POINT
			&& this.getSkillTemplate().getProperties().getTargetType() == TargetRangeAttribute.POINT)
			return true;

		return false;
	}

	public boolean isMulticast() {
		return this.isMultiCast;
	}

	public void setIsMultiCast(boolean isMultiCast) {
		this.isMultiCast = isMultiCast;
	}

	public long getCastStartTime() {
		return castStartTime;
	}

	public List<ChargedSkill> getChargeSkillList() {
		return chargeSkillList;
	}

	/**
	 * @return the instantSkill
	 */
	public boolean isInstantSkill() {
		return instantSkill;
	}
}
