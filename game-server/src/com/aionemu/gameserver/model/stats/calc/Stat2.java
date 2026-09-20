package com.aionemu.gameserver.model.stats.calc;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.stats.container.StatEnum;

/**
 * @author ATracer
 */
public abstract class Stat2 {

	protected final StatEnum stat;
	private final Creature owner;
	float base;
	float baseRate = 1f;
	float bonus;
	float bonusRate = 1f;
	float fixedBonusRate;
	float finalRate = 1f;

	public Stat2(StatEnum stat, float base, Creature owner) {
		this.stat = stat;
		this.owner = owner;
		this.base = base;
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
		return (int) getExactCurrent();
	}

	public final float getExactCurrent() {
		return (base * baseRate + bonus * bonusRate + base * fixedBonusRate) * finalRate;
	}

	public final float getExactCurrentWithoutBonus() {
		return (base * baseRate + base * fixedBonusRate) * finalRate;
	}

	public final float getExactCurrentWithoutFixedBonus() {
		return (base * baseRate + bonus * bonusRate) * finalRate;
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

	/**
	 * Rate applied to the final value (base and bonus alike), meant for situational penalties which are not part of the stat itself, like the physical
	 * defense loss while flying. Must be set after all stat functions have been applied, since caps are calculated without it.
	 */
	public final void setFinalRate(float finalRate) {
		this.finalRate = finalRate;
	}

	public final float getFinalRate() {
		return finalRate;
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
