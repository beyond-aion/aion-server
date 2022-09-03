package com.aionemu.gameserver.model.instance;

import com.aionemu.gameserver.model.Race;

/**
 * 1 Primary Armory 2 Backup Armory 3 Gravity Control 4 Engine Room 5 Auxiliary Power 6 Weapons Deck 7 Lower Weapons Deck 8 Ready Room 1 9 Ready
 * Room 2 10 Barracks 11 Logistics Managment 12 Logistics Storage 13 The Bridge 14 Captain's Room
 */
public class DredgionRoom {

	private final int roomId;
	private int state = 0xFF;

	public DredgionRoom(int roomId) {
		this.roomId = roomId;
	}

	public int getRoomId() {
		return roomId;
	}

	public void captureRoom(Race race) {
		state = race == Race.ASMODIANS ? 0x01 : 0x00;
	}

	public int getState() {
		return state;
	}
}
