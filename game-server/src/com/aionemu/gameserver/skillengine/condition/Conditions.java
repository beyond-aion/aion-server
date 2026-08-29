package com.aionemu.gameserver.skillengine.condition;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Conditions", propOrder = { "conditions" })
public class Conditions {

	@XmlElements({ @XmlElement(name = "abnormal", type = AbnormalStateCondition.class), @XmlElement(name = "target", type = TargetCondition.class),
		@XmlElement(name = "mp", type = MpCondition.class), @XmlElement(name = "hp", type = HpCondition.class),
		@XmlElement(name = "dp", type = DpCondition.class), @XmlElement(name = "move_casting", type = PlayerMovedCondition.class),
		@XmlElement(name = "ride_robot", type = RideRobotCondition.class), @XmlElement(name = "onfly", type = OnFlyCondition.class),
		@XmlElement(name = "weapon", type = WeaponCondition.class), @XmlElement(name = "noflying", type = NoFlyingCondition.class),
		@XmlElement(name = "lefthandweapon", type = LeftHandCondition.class), @XmlElement(name = "charge", type = ItemChargeCondition.class),
		@XmlElement(name = "chargeweapon", type = ChargeWeaponCondition.class), @XmlElement(name = "chargearmor", type = ChargeArmorCondition.class),
		@XmlElement(name = "polishchargeweapon", type = PolishChargeCondition.class),
		@XmlElement(name = "skillcharge", type = SkillChargeCondition.class), @XmlElement(name = "targetflying", type = TargetFlyingCondition.class),
		@XmlElement(name = "selfflying", type = SelfFlyingCondition.class), @XmlElement(name = "combatcheck", type = CombatCheckCondition.class),
		@XmlElement(name = "chain", type = ChainCondition.class), @XmlElement(name = "front", type = FrontCondition.class),
		@XmlElement(name = "back", type = BackCondition.class), @XmlElement(name = "form", type = FormCondition.class),
		@XmlElement(name = "race", type = RaceCondition.class)

	})
	protected List<Condition> conditions;

	public List<Condition> getConditions() {
		return conditions == null ? Collections.emptyList() : conditions;
	}

	public boolean validate(Skill skill) {
		for (Condition condition : getConditions()) {
			if (!condition.validate(skill)) {
				return false;
			}
		}
		return true;
	}

	public boolean canValidate(Skill skill) {
		for (Condition condition : getConditions()) {
			if (!condition.canValidate(skill)) {
				return false;
			}
		}
		return true;
	}

	public boolean validate(Stat2 stat, IStatFunction statFunction) {
		for (Condition condition : getConditions()) {
			if (!condition.validate(stat, statFunction)) {
				return false;
			}
		}
		return true;
	}

	public boolean validate(Effect effect) {
		for (Condition condition : getConditions()) {
			if (!condition.validate(effect)) {
				return false;
			}
		}
		return true;
	}
}
