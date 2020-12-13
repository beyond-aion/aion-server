package com.aionemu.gameserver.model.stats.calc.functions;

import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author kecimis
 */
public class StatAbsFunction extends StatFunction {

	private boolean debuff = false;

	public StatAbsFunction() {
	}

	public StatAbsFunction(StatEnum name, int value, boolean debuff) {
		super(name, value, false);
		this.debuff = debuff;
	}

	@Override
	public void apply(Stat2 stat, CalculationType... calculationTypes) {
		if (!isBonus()) {
			stat.setBase(getValue());
			stat.setBonus(0);
			stat.setBaseRate(1f);
		}
		// what to do with bonus?
	}

	@Override
	public final int getPriority() {
		if (debuff)
			return isBonus() ? 110 : 90;

		return isBonus() ? 100 : 80;
	}

	@Override
	public String toString() {
		return "StatAbsFunction [" + super.toString() + "]";
	}

}
