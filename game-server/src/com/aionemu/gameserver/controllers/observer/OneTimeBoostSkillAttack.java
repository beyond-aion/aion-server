package com.aionemu.gameserver.controllers.observer;

import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillType;

/**
 * Resolved values of an active one time skill attack boost. A creature can only have one of them, the last applied effect replaces the previous one.
 */
public class OneTimeBoostSkillAttack {

	private final Effect effect;
	private final SkillType type;
	private final int dmgPercent;
	private final int dmgFlat;
	private final int accBonusPercent;
	private final int accBonusFlat;
	private final AtomicInteger remainingCount;

	public OneTimeBoostSkillAttack(Effect effect, SkillType type, int dmgPercent, int dmgFlat, int accBonusPercent, int accBonusFlat, int count) {
		this.effect = effect;
		this.type = type;
		this.dmgPercent = dmgPercent;
		this.dmgFlat = dmgFlat;
		this.accBonusPercent = accBonusPercent;
		this.accBonusFlat = accBonusFlat;
		this.remainingCount = new AtomicInteger(count);
	}

	public Effect getEffect() {
		return effect;
	}

	public boolean applies(SkillType attackType) {
		return type == SkillType.ALL || type == attackType;
	}

	public float calculateDamage(float damage) {
		return damage * (100 + dmgPercent) / 100f + dmgFlat;
	}

	/**
	 * @return the accuracy this boost adds to physical attacks
	 */
	public int calculatePhysicalAccuracyBonus(Stat2 accuracy) {
		return accuracy.getBase() * accBonusPercent / 100 + accBonusFlat;
	}

	/**
	 * Unlike the physical bonus, this one is calculated from the already boosted accuracy and additionally applies the creatures bonus stats a second
	 * time, which is why it cannot be expressed as a simple addend.
	 *
	 * @return the magical accuracy to use instead of the regular one
	 */
	public int calculateMagicalAccuracy(Stat2 accuracy, int accMod) {
		int baseAcc = accuracy.getCurrent() + accMod;
		int boostedAcc = (int) (baseAcc + baseAcc * (accuracy.getFixedBonusRate() + accBonusPercent / 100f)
			+ accuracy.getExactBonus() * accuracy.getBonusRate() + accBonusFlat);
		return Math.max(0, boostedAcc);
	}

	/**
	 * Consumes one charge and ends the effect when none are left. Boosts without a damage bonus never consume charges and therefore last for their
	 * full duration.
	 */
	public void consumeCharge() {
		if (dmgPercent == 0 && dmgFlat == 0)
			return;
		if (remainingCount.decrementAndGet() <= 0)
			effect.endEffect();
	}
}
