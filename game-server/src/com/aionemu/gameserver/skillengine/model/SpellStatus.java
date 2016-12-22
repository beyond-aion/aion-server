package com.aionemu.gameserver.skillengine.model;

/**
 * @author ATracer
 */
public enum SpellStatus {

	NONE(0),
	STUMBLE(1),
	STAGGER(2), // knockback
	OPENAERIAL(4),
	CLOSEAERIAL(8),
	SPIN(16),
	BLOCK(32),
	PARRY(64),
	DODGE(128),
	DODGE2(-128), // TEMP
	RESIST(256);

	private int id;

	private SpellStatus(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
