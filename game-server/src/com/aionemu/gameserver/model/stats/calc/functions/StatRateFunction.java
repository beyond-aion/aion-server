package com.aionemu.gameserver.model.stats.calc.functions;

import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author ATracer
 */
public class StatRateFunction extends StatFunction {

	public StatRateFunction() {
	}

	public StatRateFunction(StatEnum name, int value, boolean bonus) {
		super(name, value, bonus);
	}

	@Override
	public void apply(Stat2 stat, CalculationType... calculationTypes) {
		if (isBonus()) {
			int baseValue = stat.getBaseWithoutBaseRate();
			if (getName() == StatEnum.SPEED && getValue() < 0 && stat.getBonus() < 0) { // fix to avoid run speed <= 0%
				// calculate relative to current resultValue if negative, otherwise we end up with <= 0% on multiple stat functions
				baseValue = stat.getCurrent();
			}
			stat.addToBonus(baseValue * getValue() / 100f);
		} else {
			stat.setBase(stat.getBaseWithoutBaseRate() * stat.calculatePercent(getValue()));
		}
	}

	@Override
	public final int getPriority() {
		return isBonus() ? 50 : 20;
	}

	@Override
	public String toString() {
		return "StatRateFunction [" + super.toString() + "]";
	}

}
