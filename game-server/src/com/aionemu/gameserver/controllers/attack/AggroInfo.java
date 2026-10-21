package com.aionemu.gameserver.controllers.attack;

import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * AggroInfo: - hate of creature - damage of creature
 * 
 * @author ATracer, Sarynth
 */
public class AggroInfo {

	private static final int HATE_REDUCE_VALUE = 364; // most retail npcs lose 364 hate. TODO: find formula
	private final Creature attacker;
	private int hate;
	private int damage;
	private long firstDamageTime;
	private long lastInteractionTime = 0;
	private int hateReduceCount = 1;

	AggroInfo(Creature attacker) {
		this.attacker = attacker;
	}

	public Creature getAttacker() {
		return attacker;
	}

	public void addDamage(int damage) {
		addDamage(damage, System.currentTimeMillis());
	}

	void addDamage(int damage, long time) {
		if (damage > 0) {
			if (this.damage == 0 || time < firstDamageTime)
				firstDamageTime = time;
			this.damage += damage;
		}
	}

	public void addHate(int hate) {
		this.hate += hate;
		if (this.hate < 1)
			this.hate = 1;
		lastInteractionTime = System.currentTimeMillis();
		hateReduceCount = 1;
	}

	public int getHate() {
		return this.hate;
	}

	public void setHate(int hate) {
		this.hate = hate;
	}

	public int getDamage() {
		return this.damage;
	}

	public long getFirstDamageTime() {
		return firstDamageTime;
	}

	public long getLastInteractionTime() {
		return lastInteractionTime;
	}

	void reduceHate() {
		if (hate > 1) {
			hate -= HATE_REDUCE_VALUE * hateReduceCount;
			hateReduceCount++;
			if (hate < 1)
				hate = 1;
		}
	}
}
