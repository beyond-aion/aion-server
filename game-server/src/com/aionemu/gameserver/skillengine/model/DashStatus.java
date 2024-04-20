package com.aionemu.gameserver.skillengine.model;

/**
 * @author weiwei, VladimirZ
 */
public enum DashStatus {
	NONE(0),
	RANDOMMOVELOC(1),
	DASH(2),
	BACKDASH(3),
	MOVEBEHIND(4),
	RANDOMMOVELOC_NEW(6);

	private int id;

	private DashStatus(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

}
