package com.aionemu.gameserver.skillengine.model;

import java.util.Iterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.skillengine.action.Actions;
import com.aionemu.gameserver.skillengine.condition.ChainCondition;
import com.aionemu.gameserver.skillengine.condition.Condition;
import com.aionemu.gameserver.skillengine.condition.Conditions;
import com.aionemu.gameserver.skillengine.condition.HpCondition;
import com.aionemu.gameserver.skillengine.condition.PlayerMovedCondition;
import com.aionemu.gameserver.skillengine.condition.RideRobotCondition;
import com.aionemu.gameserver.skillengine.condition.SkillChargeCondition;
import com.aionemu.gameserver.skillengine.effect.EffectTemplate;
import com.aionemu.gameserver.skillengine.effect.EffectType;
import com.aionemu.gameserver.skillengine.effect.Effects;
import com.aionemu.gameserver.skillengine.periodicaction.PeriodicActions;
import com.aionemu.gameserver.skillengine.properties.Properties;

/**
 * @author ATracer modified by Wakizashi
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "skillTemplate", propOrder = { "properties", "startconditions", "useconditions", "endconditions", "useequipmentconditions", "effects",
	"actions", "periodicActions", "motion" })
public class SkillTemplate {

	private Properties properties;
	private Conditions startconditions;
	private Conditions useconditions;
	private Conditions endconditions;
	private Conditions useequipmentconditions;
	private Effects effects;
	private Actions actions;
	@XmlElement(name = "periodicactions")
	private PeriodicActions periodicActions;
	private Motion motion;

	@XmlAttribute(name = "skill_id", required = true)
	private int skillId;
	@XmlAttribute(required = true)
	private String name;
	@XmlAttribute(required = true)
	private int nameId;
	@XmlAttribute(required = true)
	private String stack;
	@XmlAttribute(name = "group", required = false)
	private String group;
	@XmlAttribute
	private int cooldownId;
	@XmlAttribute
	private int lvl;
	@XmlAttribute(name = "skilltype", required = true)
	private SkillType type = SkillType.NONE;
	@XmlAttribute(name = "skillsubtype", required = true)
	private SkillSubType subType;
	@XmlAttribute(name = "skill_category")
	private SkillCategory skillCategory = SkillCategory.NONE;
	@XmlAttribute(name = "tslot")
	private SkillTargetSlot targetSlot;
	@XmlAttribute(name = "tslot_level")
	private int targetSlotLevel;
	@XmlAttribute(name = "dispel_category")
	private DispelCategoryType dispelCategory = DispelCategoryType.NONE;
	@XmlAttribute(name = "req_dispel_level")
	private int reqDispelLevel;
	@XmlAttribute(name = "activation", required = true)
	private ActivationAttribute activationAttribute;
	@XmlAttribute(required = true)
	private int duration;
	@XmlAttribute(name = "toggle_timer")
	private int toggleTimer;
	@XmlAttribute(name = "cooldown")
	private int cooldown;
	@XmlAttribute(name = "cooldown_delta_lv")
	private int cooldownDeltaLv;
	@XmlAttribute(name = "penalty_skill_id")
	private int penaltySkillId;
	@XmlAttribute(name = "pvp_damage")
	private int pvpDamage;
	@XmlAttribute(name = "pvp_duration")
	private int pvpDuration;
	@XmlAttribute(name = "chain_skill_prob")
	private int chainSkillProb = 100;
	@XmlAttribute(name = "cancel_rate")
	private int cancelRate;
	@XmlAttribute(name = "stance")
	private boolean stance;
	@XmlAttribute(name = "avatar")
	private boolean isDeityAvatar;
	@XmlAttribute(name = "ground")
	private boolean isGroundSkill;// TODO remove!
	@XmlAttribute(name = "ammospeed")
	private int ammoSpeed;
	@XmlAttribute(name = "conflict_id")
	private int conflictId;
	@XmlAttribute(name = "counter_skill")
	private AttackStatus counterSkill = null;
	@XmlAttribute(name = "noremoveatdie")
	private boolean noRemoveAtDie = false;
	@XmlAttribute(name = "no_save_on_logout")
	private boolean noSaveOnLogout = false;
	@XmlAttribute(name = "stigma")
	private StigmaType stigmaType = StigmaType.NONE;

	/**
	 * @return the Properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * Gets the value of the startconditions property.
	 * 
	 * @return possible object is {@link Conditions }
	 */
	public Conditions getStartconditions() {
		return startconditions;
	}

	/**
	 * Gets the value of the useconditions property.
	 * 
	 * @return possible object is {@link Conditions }
	 */
	public Conditions getUseconditions() {
		return useconditions;
	}

	/**
	 * Gets the value of the useequipmentconditions property.
	 * 
	 * @return possible object is {@link Conditions }
	 */
	public Conditions getUseEquipmentconditions() {
		return useequipmentconditions;
	}

	/**
	 * Gets the value of the effects property.
	 * 
	 * @return possible object is {@link Effects }
	 */
	public Effects getEffects() {
		return effects;
	}

	/**
	 * Gets the value of the actions property.
	 * 
	 * @return possible object is {@link Actions }
	 */
	public Actions getActions() {
		return actions;
	}

	/**
	 * Gets the value of the periodicActions property.
	 * 
	 * @return possible object is {@link PeriodicActions }
	 */
	public PeriodicActions getPeriodicActions() {
		return periodicActions;
	}

	/**
	 * Gets the value of the motion property.
	 * 
	 * @return possible object is {@link Motion }
	 */
	public Motion getMotion() {
		return motion;
	}

	/**
	 * Gets the value of the skillId property.
	 */
	public int getSkillId() {
		return skillId;
	}

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the nameId
	 */
	public int getNameId() {
		return nameId;
	}

	/**
	 * @return the stack
	 */
	public String getStack() {
		return stack;
	}

	/**
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * @return the lvl
	 */
	public int getLvl() {
		return lvl;
	}

	/**
	 * Gets the value of the type property.
	 * 
	 * @return possible object is {@link SkillType }
	 */
	public SkillType getType() {
		return type;
	}

	/**
	 * @return the subType
	 */
	public SkillSubType getSubType() {
		return subType;
	}

	/**
	 * @return the targetSlot
	 */
	public SkillTargetSlot getTargetSlot() {
		return targetSlot;
	}

	/**
	 * @return the targetSlot Level
	 */
	public int getTargetSlotLevel() {
		return targetSlotLevel;
	}

	/**
	 * @return the dispelCategory
	 */
	public DispelCategoryType getDispelCategory() {
		return dispelCategory;
	}

	/**
	 * @return the reqDispelLevel
	 */
	public int getReqDispelLevel() {
		return reqDispelLevel;
	}

	/**
	 * @return the duration
	 */
	public int getDuration() {
		return duration;
	}

	public int getToggleTimer() {
		return toggleTimer;
	}

	/**
	 * @return the stigmaType
	 */
	public StigmaType getStigmaType() {
		return stigmaType;
	}

	/**
	 * @return the activationAttribute
	 */
	public ActivationAttribute getActivationAttribute() {
		return activationAttribute;
	}

	public boolean isPassive() {
		return activationAttribute == ActivationAttribute.PASSIVE;
	}

	public boolean isToggle() {
		return activationAttribute == ActivationAttribute.TOGGLE;
	}

	public boolean isProvoked() {
		return activationAttribute == ActivationAttribute.PROVOKED;
	}

	public boolean isMaintain() {
		return activationAttribute == ActivationAttribute.MAINTAIN;
	}

	public boolean isActive() {
		return activationAttribute == ActivationAttribute.ACTIVE;
	}

	public boolean isCharge() {
		return activationAttribute == ActivationAttribute.CHARGE;
	}

	/**
	 * @param position
	 * @return EffectTemplate
	 */
	public EffectTemplate getEffectTemplate(int position) {
		return effects != null && effects.getEffects().size() >= position ? effects.getEffects().get(position - 1) : null;

	}

	/**
	 * @return the cooldown
	 */
	public int getCooldown() {
		return cooldown;
	}

	public int getCooldownDeltaLv() {
		return cooldownDeltaLv;
	}

	/**
	 * @return the penaltySkillId
	 */
	public int getPenaltySkillId() {
		return penaltySkillId;
	}

	/**
	 * @return the pvpDamage
	 */
	public int getPvpDamage() {
		return pvpDamage;
	}

	/**
	 * @return the pvpDuration
	 */
	public int getPvpDuration() {
		return pvpDuration;
	}

	/**
	 * @return chainSkillProb
	 */
	public int getChainSkillProb() {
		return chainSkillProb;
	}

	/**
	 * @return cancelRate
	 */
	public int getCancelRate() {
		return cancelRate;
	}

	/**
	 * @return stance
	 */
	public boolean isStance() {
		return stance;
	}

	public boolean hasAnyEffect(EffectType... effectTypes) {
		return hasAnyEffect(false, effectTypes);
	}

	public boolean hasAnyEffect(boolean checkSubEffects, EffectType... effectTypes) {
		if (effects == null)
			return false;
		if (effects.hasAnyEffectType(effectTypes))
			return true;
		if (checkSubEffects) {
			for (EffectTemplate et : effects.getEffects()) {
				if (et.getSubEffect() != null) {
					if (DataManager.SKILL_DATA.getSkillTemplate(et.getSubEffect().getSkillId()).hasAnyEffect(effectTypes)) // should we check recursively?
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return resurrectbase is excluded because of different behavior
	 */
	public boolean hasResurrectEffect() {
		return hasAnyEffect(EffectType.RESURRECT, EffectType.RESURRECTPOSITIONAL);
	}

	public boolean hasEvadeEffect() {
		return hasAnyEffect(EffectType.EVADE);
	}

	public boolean hasRecallInstant() {
		return hasAnyEffect(EffectType.RECALLINSTANT);
	}

	public int getCooldownId() {
		return (cooldownId > 0) ? cooldownId : skillId;
	}

	public boolean isDeityAvatar() {
		return isDeityAvatar;
	}

	public boolean isGroundSkill() {
		return isGroundSkill;
	}

	public AttackStatus getCounterSkill() {
		return counterSkill;
	}

	public int getAmmoSpeed() {
		return ammoSpeed;
	}

	public int getConflictId() {
		return conflictId;
	}

	public boolean isNoRemoveAtDie() {
		return noRemoveAtDie;
	}

	public boolean isNoSaveOnLogout() {
		return noSaveOnLogout;
	}

	public int getEffectsDuration(int skillLevel) {
		int duration = 0;
		Iterator<EffectTemplate> itr = getEffects().getEffects().iterator();
		while (itr.hasNext() && duration == 0) {
			EffectTemplate et = itr.next();
			int effectDuration = et.getDuration2() + et.getDuration1() * skillLevel;
			if (et.getRandomTime() > 0)
				effectDuration -= Rnd.get(0, et.getRandomTime());
			duration = duration > effectDuration ? duration : effectDuration;
		}

		return duration;
	}

	public ChainCondition getChainCondition() {
		if (startconditions != null) {
			for (Condition cond : startconditions.getConditions()) {
				if (cond instanceof ChainCondition)
					return (ChainCondition) cond;
			}
		}

		return null;
	}

	public RideRobotCondition getRideRobotCondition() {
		if (useconditions != null) {
			for (Condition cond : useconditions.getConditions()) {
				if (cond instanceof RideRobotCondition)
					return (RideRobotCondition) cond;
			}
		}
		return null;
	}

	public SkillChargeCondition getSkillChargeCondition() {
		if (startconditions != null) {
			for (Condition cond : startconditions.getConditions()) {
				if (cond instanceof SkillChargeCondition)
					return (SkillChargeCondition) cond;
			}
		}

		return null;
	}

	/**
	 * @return
	 */
	public HpCondition getHpCondition() {
		for (Condition c : startconditions.getConditions())
			if (c instanceof HpCondition) {
				return ((HpCondition) c);
			}
		return null;
	}

	public PlayerMovedCondition getMovedCondition() {
		for (Condition c : startconditions.getConditions())
			if (c instanceof PlayerMovedCondition) {
				return ((PlayerMovedCondition) c);
			}
		return null;
	}

	public Conditions getEndConditions() {
		return endconditions;
	}

	public SkillCategory getSkillCategory() {
		return skillCategory;
	}

}
