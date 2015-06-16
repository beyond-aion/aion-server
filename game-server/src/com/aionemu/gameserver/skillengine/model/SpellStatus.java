package com.aionemu.gameserver.skillengine.model;

/**
 * @author ATracer
 */
public enum SpellStatus {
	/**
	 * Spell Status 1 : stumble 2 : knockback 4 : open aerial 8 : close aerial 16 : spin 32 : block 64 : parry 128 : dodge
	 * 256 : resist
	 */

	NONE(0),
	STUMBLE(1),
	STAGGER(2),
	OPENAERIAL(4),
	CLOSEAERIAL(8),
	SPIN(16),
	BLOCK(32),
	PARRY(64),
	DODGE(128),
	DODGE2(-128),//TEMP
	RESIST(256);

	private int id;

	private SpellStatus(int id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
}
