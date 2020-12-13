package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author ATracer
 */
public class WeaponStats {

	@XmlAttribute(name = "min_damage")
	protected int minDamage;
	@XmlAttribute(name = "max_damage")
	protected int maxDamage;
	@XmlAttribute(name = "attack_speed")
	protected int attackSpeed;
	@XmlAttribute(name = "critical")
	protected int critical;
	@XmlAttribute(name = "physical_accuracy")
	protected int physicalAccuracy;
	@XmlAttribute
	protected int parry;
	@XmlAttribute(name = "magical_accuracy")
	protected int magicalAccuracy;
	@XmlAttribute(name = "boost_magical_skill")
	protected int boostMagicalSkill;
	@XmlAttribute(name = "attack_range")
	protected int attackRange;
	@XmlAttribute(name = "hit_count")
	protected int hitCount;
	@XmlAttribute(name = "reduce_max")
	protected int reduceMax;

	public final int getMinDamage() {
		return minDamage;
	}

	public final int getMaxDamage() {
		return maxDamage;
	}

	public final float getMeanDamage() {
		return (minDamage + maxDamage) / 2f;
	}

	public final int getAttackSpeed() {
		return attackSpeed;
	}

	public final int getCritical() {
		return critical;
	}

	public final int getPhysicalAccuracy() {
		return physicalAccuracy;
	}

	public final int getParry() {
		return parry;
	}

	public final int getMagicalAccuracy() {
		return magicalAccuracy;
	}

	public final int getBoostMagicalSkill() {
		return boostMagicalSkill;
	}

	public final int getAttackRange() {
		return attackRange;
	}

	public final int getHitCount() {
		return hitCount;
	}

	public final int getReduceMax() {
		return reduceMax;
	}

}
