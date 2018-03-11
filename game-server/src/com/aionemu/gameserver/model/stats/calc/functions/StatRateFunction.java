package com.aionemu.gameserver.model.stats.calc.functions;

import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;

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
	public void apply(Stat2 stat) {
		if (isBonus()) {
			// calculate relative to current resultValue if negative, otherwise we end up with <= 0% with multiple stat functions
			int baseValue = getValue() < 0 && stat.getBonus() < 0 ? stat.getCurrent() : stat.getBaseWithoutBaseRate();
			stat.addToBonus((int) (baseValue * getValue() / 100f));
		} else {
			stat.setBase((int) (stat.getBaseWithoutBaseRate() * stat.calculatePercent(getValue())));
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
