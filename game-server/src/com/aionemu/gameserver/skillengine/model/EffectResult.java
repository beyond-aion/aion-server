package com.aionemu.gameserver.skillengine.model;

/**
 * @author Cheatkiller
 */
public enum EffectResult {

	NORMAL(0),
	ABSORBED(1),
	CONFLICT(2),
	DODGE(3),
	RESIST(4),
	IMMUNE(5), // TODO: IMPLEMENT
	CANCELED_DUE_TO_TOO_MANY_EFFECTS(6);

	private int id;

	private EffectResult(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
