package com.aionemu.gameserver.model.flypath;

/**
 * @author xTz
 */
public enum FlyPathType {

	WIND_BOX(0), // a column that lifts gliding players (such as geysers)
	WIND_PATH(1), // a windstream route
	MOVING_COLLISION(2); // a lift or another moving platform

	private int id;

	private FlyPathType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
