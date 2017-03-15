package com.aionemu.gameserver.model.gameobjects.player;

import java.util.LinkedHashMap;

/**
 * @author Rolandas
 * @modified Neon
 */
public class HouseObjectCooldownList {

	private LinkedHashMap<Integer, Long> houseObjectCooldowns;

	public HouseObjectCooldownList() {
	}

	public boolean isCanUseObject(int objectId) {
		long coolDown = getHouseObjectCooldown(objectId);
		if (coolDown > System.currentTimeMillis())
			return false;

		if (coolDown != 0)
			houseObjectCooldowns.remove(objectId);

		return true;
	}

	public long getHouseObjectCooldown(int objectId) {
		if (houseObjectCooldowns == null || !houseObjectCooldowns.containsKey(objectId))
			return 0;

		return houseObjectCooldowns.get(objectId);
	}

	public LinkedHashMap<Integer, Long> getHouseObjectCooldowns() {
		return houseObjectCooldowns;
	}

	public void setHouseObjectCooldown(int objectId, long reuseTime) {
		if (houseObjectCooldowns == null)
			houseObjectCooldowns = new LinkedHashMap<>();

		if (reuseTime > 0)
			houseObjectCooldowns.put(objectId, reuseTime);
		else
			houseObjectCooldowns.remove(objectId);
	}

	public int getReuseDelay(int objectId) {
		if (isCanUseObject(objectId))
			return 0;
		long cd = getHouseObjectCooldown(objectId);
		int delay = (int) ((cd - System.currentTimeMillis()) / 1000);
		return delay;
	}
}
