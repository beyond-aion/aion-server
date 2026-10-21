package com.aionemu.gameserver.controllers.attack;

import com.aionemu.gameserver.model.gameobjects.AionObject;

public class DamageInfo<T extends AionObject> {

	private final T attacker;
	private int damage;
	private long firstDamageTime;

	public DamageInfo(T attacker) {
		this.attacker = attacker;
	}

	public T getAttacker() {
		return attacker;
	}

	public int getDamage() {
		return damage;
	}

	public long getFirstDamageTime() {
		return firstDamageTime;
	}

	void addDamage(int damage, long firstDamageTime) {
		if (this.damage == 0 || firstDamageTime < this.firstDamageTime)
			this.firstDamageTime = firstDamageTime;
		this.damage += damage;
	}
}
