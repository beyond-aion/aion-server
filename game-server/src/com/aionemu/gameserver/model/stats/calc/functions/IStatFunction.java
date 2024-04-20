package com.aionemu.gameserver.model.stats.calc.functions;

import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author ATracer, Rolandas
 */
public interface IStatFunction extends Comparable<IStatFunction> {

	StatEnum getName();

	boolean isBonus();

	int getPriority();

	int getValue();

	boolean validate(Stat2 stat);

	void apply(Stat2 stat, CalculationType... calculationTypes);

	StatOwner getOwner();

	boolean hasConditions();

	boolean hasNullOwner();

}
