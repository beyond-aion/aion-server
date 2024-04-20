package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.L10n;
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
 * @author ATracer, Wakizashi
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "skillTemplate", propOrder = { "properties", "startconditions", "useconditions", "endconditions", "useequipmentconditions", "effects",
	"actions", "periodicActions", "motion" })
public class SkillTemplate implements L10n {

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
	@XmlAttribute(name = "penalty_skill_send_msg")
	private boolean penaltySkillSendMsg = false;
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
	private boolean noRemoveOnDie = false;
	@XmlAttribute(name = "no_save_on_logout")
	private boolean noSaveOnLogout = false;
	@XmlAttribute(name = "stigma")
	private StigmaType stigmaType = StigmaType.NONE;
	@XmlAttribute(name = "applymcrit")
	private boolean applyMcrit = true;
	@XmlAttribute(name = "hostile_type")
	private HostileType hostileType = HostileType.NONE;

	public Properties getProperties() {
		return properties;
	}

	public Conditions getStartconditions() {
		return startconditions;
	}

	public Conditions getUseconditions() {
		return useconditions;
	}

	public Conditions getUseEquipmentconditions() {
		return useequipmentconditions;
	}

	public Effects getEffects() {
		return effects;
	}

	public Actions getActions() {
		return actions;
	}

	public PeriodicActions getPeriodicActions() {
		return periodicActions;
	}

	public Motion getMotion() {
		return motion;
	}

	public int getSkillId() {
		return skillId;
	}

	public String getName() {
		return name;
	}

	@Override
	public int getL10nId() {
		return nameId;
	}

	public String getStack() {
		return stack;
	}

	public String getGroup() {
		return group;
	}

	public int getLvl() {
		return lvl;
	}

	public SkillType getType() {
		return type;
	}

	public SkillSubType getSubType() {
		return subType;
	}

	public SkillTargetSlot getTargetSlot() {
		return targetSlot;
	}

	public int getTargetSlotLevel() {
		return targetSlotLevel;
	}

	public DispelCategoryType getDispelCategory() {
		return dispelCategory;
	}

	public int getReqDispelLevel() {
		return reqDispelLevel;
	}

	public int getDuration() {
		return duration;
	}

	public int getToggleTimer() {
		return toggleTimer;
	}

	public StigmaType getStigmaType() {
		return stigmaType;
	}

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

	public EffectTemplate getEffectTemplate(int position) {
		return effects != null && effects.getEffects().size() >= position ? effects.getEffects().get(position - 1) : null;

	}

	public int getCooldown() {
		return cooldown;
	}

	public int getCooldownDeltaLv() {
		return cooldownDeltaLv;
	}

	public int getPenaltySkillId() {
		return penaltySkillId;
	}

	public boolean shouldPenaltySkillSendMsg() {
		return penaltySkillSendMsg;
	}

	public int getPvpDamage() {
		return pvpDamage;
	}

	public int getPvpDuration() {
		return pvpDuration;
	}

	public int getChainSkillProb() {
		return chainSkillProb;
	}

	public int getCancelRate() {
		return cancelRate;
	}

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

	public boolean isNoRemoveOnDie() {
		return noRemoveOnDie;
	}

	public boolean isNoSaveOnLogout() {
		return noSaveOnLogout;
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

	public boolean isMcritApplied() {
		return applyMcrit;
	}

	public HostileType getHostileType() {
		return hostileType;
	}
}
