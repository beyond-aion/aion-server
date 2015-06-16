package com.aionemu.gameserver.controllers.attack;

import com.aionemu.gameserver.model.gameobjects.AionObject;

/**
 * AggroInfo: - hate of creature - damage of creature
 * 
 * @author ATracer, Sarynth
 */
public class AggroInfo {

	private AionObject attacker;
	private int hate;
	private int damage;

	/**
	 * @param attacker
	 */
	AggroInfo(AionObject attacker) {
		this.attacker = attacker;
	}

	/**
	 * @return attacker
	 */
	public AionObject getAttacker() {
		return attacker;
	}

	/**
	 * @param damage
	 */
	public void addDamage(int damageValue) {
		this.damage += damageValue;
		if (this.damage < 0)
			this.damage = 0;
	}

	/**
	 * @param damage
	 */
	public void addHate(int damageValue) {
		this.hate += damageValue;
		if (this.hate < 1)
			this.hate = 1;
	}

	/**
	 * @return hate
	 */
	public int getHate() {
		return this.hate;
	}

	/**
	 * @param hate
	 */
	public void setHate(int hate) {
		this.hate = hate;
	}

	/**
	 * @return damage
	 */
	public int getDamage() {
		return this.damage;
	}

	/**
	 * @param damage
	 */
	public void setDamage(int damage) {
		this.damage = damage;
	}
}
