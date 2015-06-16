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
			stat.addToBonus((int) (stat.getBase() * getValue() / 100f));
		}
		else {
			stat.setBase((int) (stat.getBase() * stat.calculatePercent(getValue())));
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
