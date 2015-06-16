package com.aionemu.gameserver.model.stats.calc.functions;

import com.aionemu.gameserver.model.stats.calc.Stat2;

/**
 * @author ATracer
 */
public class StatSubFunction extends StatFunction {

	@Override
	public void apply(Stat2 stat) {
		if (isBonus()) {
			stat.addToBonus(-getValue());
		}
		else {
			stat.addToBase(-getValue());
		}
	}

	@Override
	public final int getPriority() {
		return isBonus() ? 60 : 30;
	}

}
