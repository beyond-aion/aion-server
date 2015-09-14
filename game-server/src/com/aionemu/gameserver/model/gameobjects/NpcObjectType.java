package com.aionemu.gameserver.model.gameobjects;

/**
 * @author ATracer
 */
public enum NpcObjectType {
	NORMAL(1),
	SUMMON(2),
	HOMING(16),
	TRAP(32),
	SKILLAREA(64),
	TOTEM(128), // TODO not implemented
	GROUPGATE(256),
	SERVANT(1024),
	PET(2048);// TODO not used

	private NpcObjectType(int id) {
		this.id = id;
	}

	private int id;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
}
