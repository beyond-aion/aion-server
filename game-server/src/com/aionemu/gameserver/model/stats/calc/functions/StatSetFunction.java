package com.aionemu.gameserver.model.stats.calc.functions;

import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author ATracer
 */
public class StatSetFunction extends StatFunction {

	public StatSetFunction() {
	}

	public StatSetFunction(StatEnum name, int value) {
		super(name, value, false);
	}

	@Override
	public void apply(Stat2 stat, CalculationType... calculationTypes) {
		if (isBonus())
			stat.setBonus(getValue());
		else
			stat.setBase(getValue());
	}

	@Override
	public final int getPriority() {
		return isBonus() ? 70 : 40;
	}

	@Override
	public String toString() {
		return "StatSetFunction [" + super.toString() + "]";
	}

}
