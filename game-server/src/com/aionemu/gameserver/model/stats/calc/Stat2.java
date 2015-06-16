package com.aionemu.gameserver.model.stats.calc;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.stats.container.StatEnum;

/**
 * @author ATracer
 */
public abstract class Stat2 {

	float bonusRate = 1f;
	float baseRate = 1f;
	int base;
	int bonus;
	private final Creature owner;
	protected final StatEnum stat;

	public Stat2(StatEnum stat, int base, Creature owner) {
		this(stat, base, owner, 1);
	}

	public Stat2(StatEnum stat, int base, Creature owner, float bonusRate) {
		this.stat = stat;
		this.base = base;
		this.owner = owner;
		this.bonusRate = bonusRate;
	}

	public final StatEnum getStat() {
		return stat;
	}

	public final int getBase() {
		return (int) (base * this.getBaseRate());
	}

	public final void setBase(int base) {
		this.base = base;
	}
	
	public final float getBaseRate() {
		return baseRate;
	}

	public final void setBaseRate(float rate) {
		this.baseRate = rate;
	}

	public abstract void addToBase(int base);

	public final int getBonus() {
		return bonus;
	}

	public final int getCurrent() {
		return (int) (this.base * baseRate + this.bonus);

	}

	public final void setBonus(int bonus) {
		this.bonus = bonus;
	}

	public final float getBonusRate() {
		return bonusRate;
	}

	public final void setBonusRate(float bonusRate) {
		this.bonusRate = bonusRate;
	}

	public abstract void addToBonus(int bonus);
	
	public abstract float calculatePercent(int delta);

	public final Creature getOwner() {
		return owner;
	}

	@Override
	public String toString() {
		return "[" + stat.name() + " base=" + base + ", bonus=" + bonus + "]";
	}

}
