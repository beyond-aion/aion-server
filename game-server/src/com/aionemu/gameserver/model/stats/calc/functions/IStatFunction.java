package com.aionemu.gameserver.model.stats.calc.functions;

import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.container.StatEnum;

/**
 * @author ATracer
 * @modified Rolandas
 */
public interface IStatFunction extends Comparable<IStatFunction> {

	StatEnum getName();

	boolean isBonus();

	int getPriority();

	int getValue();

	boolean validate(Stat2 stat);

	void apply(Stat2 stat);

	StatOwner getOwner();

	boolean hasConditions();

	boolean hasNullOwner();

}
