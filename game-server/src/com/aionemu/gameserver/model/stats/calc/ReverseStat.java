package com.aionemu.gameserver.model.stats.calc;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.stats.container.StatEnum;

/**
 * @author ATracer
 */
public class ReverseStat extends Stat2 {

	public ReverseStat(StatEnum stat, int base, Creature owner) {
		super(stat, base, owner);
	}

	public ReverseStat(StatEnum stat, int base, Creature owner, float bonusRate) {
		super(stat, base, owner, bonusRate);
	}

	@Override
	public void addToBase(int base) {
		this.base -= base;
		if (this.base < 0) {
			this.base = 0;
		}
	}

	@Override
	public void addToBonus(int bonus) {
		this.bonus -= bonusRate * bonus;
	}

	@Override
	public float calculatePercent(int delta) {
		float percent = (100 - delta) / 100f;
		// TODO need double check here for negatives
		return percent < 0 ? 0 : percent;
	}

}
