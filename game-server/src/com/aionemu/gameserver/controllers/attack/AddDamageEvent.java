package com.aionemu.gameserver.controllers.attack;

import com.aionemu.gameserver.events.AbstractEvent;
import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author Rolandas
 */
public class AddDamageEvent extends AbstractEvent<AggroList> {

	private static final long serialVersionUID = -4421622095402153276L;

	private final int damage;
	private final Creature attacker;

	public AddDamageEvent(AggroList source, Creature attacker, int damage) {
		super(source);
		this.damage = damage;
		this.attacker = attacker;
	}

	public int getDamage() {
		return damage;
	}

	public Creature getAttacker() {
		return attacker;
	}

}
