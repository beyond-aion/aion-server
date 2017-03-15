package com.aionemu.gameserver.model.gameobjects.player;

import java.util.HashMap;
import java.util.Map;

/**
 * @author synchro2
 */
public class CraftCooldownList {

	private Map<Integer, Long> craftCooldowns;

	CraftCooldownList(Player owner) {
	}

	public boolean isCanCraft(int delayId) {
		if (craftCooldowns == null || !craftCooldowns.containsKey(delayId))
			return true;

		Long coolDown = craftCooldowns.get(delayId);
		if (coolDown == null)
			return true;

		if (coolDown < System.currentTimeMillis()) {
			craftCooldowns.remove(delayId);
			return true;
		}

		return false;
	}

	public long getCraftCooldown(int delayId) {
		if (craftCooldowns == null || !craftCooldowns.containsKey(delayId))
			return 0;

		return craftCooldowns.get(delayId);
	}

	public Map<Integer, Long> getCraftCoolDowns() {
		return craftCooldowns;
	}

	public void setCraftCoolDowns(Map<Integer, Long> craftCoolDowns) {
		this.craftCooldowns = craftCoolDowns;
	}

	public void addCraftCooldown(int delayId, int delay) {
		if (craftCooldowns == null) {
			craftCooldowns = new HashMap<>();
		}

		long nextUseTime = System.currentTimeMillis() + (delay * 1000);
		craftCooldowns.put(delayId, nextUseTime);
	}
}
