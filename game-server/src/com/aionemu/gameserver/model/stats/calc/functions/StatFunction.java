package com.aionemu.gameserver.model.stats.calc.functions;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.condition.Conditions;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SimpleModifier")
public class StatFunction implements IStatFunction {

	private static ConcurrentHashMap<StatEnum, Boolean> nullOwnerStats = new ConcurrentHashMap<>();

	@XmlAttribute(name = "name")
	protected StatEnum stat;
	@XmlAttribute
	private boolean bonus;
	@XmlAttribute
	protected int value;
	@XmlElement(name = "conditions")
	private Conditions conditions;
	@XmlTransient
	private int rndNumber;

	public StatFunction() {
	}

	public StatFunction(StatEnum stat, int value, boolean bonus) {
		this.stat = stat;
		this.value = value;
		this.bonus = bonus;
	}

	@Override
	public int compareTo(IStatFunction o) {
		int result = getPriority() - o.getPriority();
		if (result == 0)
			return this.hashCode() - o.hashCode();
		return result;
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
	 * priorities
	 * RATE 20
	 * ADD  30
	 * SUB  30
	 * SET  40
	 * RATE bonus 50
	 * ADD bonus 60
	 * SUB bonus 60
	 * SET bonus 70
	 * ABS 80
	 * ABS debuff 90
	 * ABS bonus 100
	 * ABS debuff bonus 110
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
		return conditions != null ? conditions.validate(stat, this) : true;
	}

	protected boolean validate(Stat2 stat, IStatFunction statFunction) {
		return conditions != null ? conditions.validate(stat, statFunction) : true;
	}

	@Override
	public void apply(Stat2 stat) {
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

	@Override
	public int getRandomNumber() {
		return rndNumber;
	}

	public void setRandomNumber(int rndNumber) {
		this.rndNumber = rndNumber;
	}

	@Override
	public boolean hasNullOwner() {
		if (nullOwnerStats.containsKey(getName()))
			return nullOwnerStats.get(getName());

		List<IStatFunction> globalStats = PlayerStatFunctions.getFunctions();
		boolean hasNullOwner = false;
		for (IStatFunction func : globalStats) {
			if (func.getName() == getName()) {
				hasNullOwner = true;
				break;
			}
		}
		nullOwnerStats.put(getName(), Boolean.valueOf(hasNullOwner));
		return hasNullOwner;
	}

}
