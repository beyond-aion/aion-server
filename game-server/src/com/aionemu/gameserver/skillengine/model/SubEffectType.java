package com.aionemu.gameserver.skillengine.model;

/**
 * @author Yeats
 */
public enum SubEffectType {

	NONE(0),
	SPIN(0),
	PULL(2),
	PULL_NPC(6),
	STUMBLE(4),
	STAGGER(4),
	OPENAERIAL(4),
	SIMPLE_MOVE_BACK(12);


	private byte id;

	private SubEffectType(int id) {
		this.id = (byte) id;
	}

	/**
	 * @return the id
	 */
	public byte getId() {
		return id;
	}
}
