package com.aionemu.gameserver.model.stats.calc;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.stats.container.StatEnum;

/**
 * @author ATracer
 */
public abstract class Stat2 {

	float bonusRate;
	float baseRate = 1f;
	float base;
	float bonus;
	float fixedBonusRate;
	private final Creature owner;
	protected final StatEnum stat;

	public Stat2(StatEnum stat, float base, Creature owner) {
		this(stat, base, owner, 1);
	}

	public Stat2(StatEnum stat, float base, Creature owner, float bonusRate) {
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

	public final int getBaseWithoutBaseRate() {
		return (int) base;
	}

	public final float getExactBaseWithoutBaseRate() {
		return base;
	}

	public float getExactBonus() {
		return bonus;
	}

	public final void setBase(float base) {
		this.base = base;
	}

	public final float getBaseRate() {
		return baseRate;
	}

	public final void setBaseRate(float rate) {
		this.baseRate = rate;
	}

	public abstract void addToBase(float base);

	public final int getBonus() {
		return (int) bonus;
	}

	public final int getCurrent() {
		return (int) (base * baseRate + bonus * bonusRate + base * fixedBonusRate);
	}

	public final float getExactCurrent() {
		return base * baseRate + bonus * bonusRate + base * fixedBonusRate;
	}

	public final float getExactCurrentWithoutFixedBonus() {
		return base * baseRate + bonus * bonusRate;
	}

	public final void setBonus(float bonus) {
		this.bonus = bonus;
	}

	public final float getBonusRate() {
		return bonusRate;
	}

	public final void setBonusRate(float bonusRate) {
		this.bonusRate = bonusRate;
	}

	public abstract void addToBonus(float bonus);

	public void setFixedBonusRate(float fixedBonusRate) {
		this.fixedBonusRate = fixedBonusRate;
	}

	public float getFixedBonusRate() {
		return fixedBonusRate;
	}

	public abstract float calculatePercent(int delta);

	public final Creature getOwner() {
		return owner;
	}

	@Override
	public String toString() {
		return "[" + stat.name() + " base=" + base + ", bonus=" + bonus + "]";
	}

}
