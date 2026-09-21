package com.aionemu.gameserver.model.stats.calc.functions;

import java.util.Set;

import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.condition.Conditions;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SimpleModifier")
public class StatFunction implements IStatFunction {

	@XmlAttribute(name = "name")
	protected StatEnum stat;
	@XmlAttribute
	private boolean bonus;
	@XmlAttribute
	protected int value;
	@XmlElement(name = "conditions")
	private Conditions conditions;

	public StatFunction() {
	}

	public StatFunction(StatEnum stat, int value, boolean bonus) {
		this.stat = stat;
		this.value = value;
		this.bonus = bonus;
	}

	@Override
	public StatOwner getOwner() {
		return null;
	}

	@Override
	public final StatEnum getName() {
		return stat;
	}

	@Override
	public final boolean isBonus() {
		return bonus;
	}

	/**
	 * priorities RATE 20 ADD 30 SUB 30 SET 40 RATE bonus 50 ADD bonus 60 SUB bonus 60 SET bonus 70 ABS 80 ABS debuff 90 ABS bonus 100 ABS debuff bonus
	 * 110
	 */
	@Override
	public int getPriority() {
		return 0x10;
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public boolean validate(Stat2 stat) {
		return conditions == null || conditions.validate(stat, this);
	}

	protected boolean validate(Stat2 stat, IStatFunction statFunction) {
		return conditions == null || conditions.validate(stat, statFunction);
	}

	@Override
	public void apply(Stat2 stat, Set<CalculationType> calculationTypes) {
	}

	@Override
	public String toString() {
		return "stat=" + stat + ", bonus=" + bonus + ", value=" + value + ", priority=" + getPriority();
	}

	public StatFunction withConditions(Conditions conditions) {
		this.conditions = conditions;
		return this;
	}

	@Override
	public boolean hasConditions() {
		return conditions != null;
	}
}
