package com.aionemu.gameserver.model.flypath;

/**
 * @author xTz
 */
public enum FlyPathType {

	GEYSER(0),
	ONE_WAY(1),
	TWO_WAY(2);

	private int id;

	private FlyPathType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
