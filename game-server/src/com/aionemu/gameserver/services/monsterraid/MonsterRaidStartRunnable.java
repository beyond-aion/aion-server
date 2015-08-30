package com.aionemu.gameserver.services.monsterraid;

import com.aionemu.gameserver.services.MonsterRaidService;


/**
 * @author Whoop
 *
 */
public class MonsterRaidStartRunnable implements Runnable {

	private final int locationId;

	public MonsterRaidStartRunnable(int locationId) {
		this.locationId = locationId;
	}

	@Override
	public void run() {
		MonsterRaidService.getInstance().checkRaidStart(getLocationId());
	}

	public int getLocationId() {
		return locationId;
	}
}
