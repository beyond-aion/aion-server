package com.aionemu.gameserver.model.stats.calc.functions;

import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author ATracer
 */
public class StatFunctionProxy implements IStatFunction, Comparable<IStatFunction> {

	private final StatOwner owner;
	private final IStatFunction proxiedFunction;

	public StatFunctionProxy(StatOwner owner, IStatFunction statFunction) {
		this.owner = owner;
		this.proxiedFunction = statFunction;
	}

	public IStatFunction getProxiedFunction() {
		return proxiedFunction;
	}

	@Override
	public StatOwner getOwner() {
		return owner;
	}

	@Override
	public StatEnum getName() {
		return proxiedFunction.getName();
	}

	@Override
	public boolean isBonus() {
		return proxiedFunction.isBonus();
	}

	@Override
	public int getPriority() {
		return proxiedFunction.getPriority();
	}

	@Override
	public int getValue() {
		return proxiedFunction.getValue();
	}

	@Override
	public boolean validate(Stat2 stat) {
		return ((StatFunction) proxiedFunction).validate(stat, this);
	}

	@Override
	public void apply(Stat2 stat, CalculationType... calculationTypes) {
		proxiedFunction.apply(stat, calculationTypes);
	}

	@Override
	public boolean hasConditions() {
		return proxiedFunction.hasConditions();
	}

	@Override
	public String toString() {
		return "Proxy [name=" + proxiedFunction.getName() + ", bonus=" + isBonus() + ", value=" + getValue() + ", priority=" + getPriority() + ", owner="
			+ owner + "]";
	}
}
