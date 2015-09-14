package com.aionemu.gameserver.model.stats.calc;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.stats.container.StatEnum;

/**
 * @author ATracer
 */
public class AdditionStat extends Stat2 {

	public AdditionStat(StatEnum stat, int base, Creature owner) {
		super(stat, base, owner);
	}

	public AdditionStat(StatEnum stat, int base, Creature owner, float bonusRate) {
		super(stat, base, owner, bonusRate);
	}

	@Override
	public final void addToBase(int base) {
		this.base += base;
	}

	@Override
	public final void addToBonus(int bonus) {
		this.bonus += bonusRate * bonus;
	}

	@Override
	public float calculatePercent(int delta) {
		return (100 + delta) / 100f;
	}

}
