package com.aionemu.gameserver.model.gameobjects.player;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Neon
 */
public class Cooldowns extends ConcurrentHashMap<Integer, Long> {

	private static final long serialVersionUID = 7043073167775715367L;

	/**
	 * @return The time (in millis) when the cooldown for given ID expires. Null if there is no active cooldown.
	 */
	@Override
	public Long get(Object cooldownId) {
		Long cd = super.get(cooldownId);
		if (cd != null && cd <= System.currentTimeMillis()) {
			if (remove(cooldownId, cd))
				return null;
			cd = get(cooldownId); // get again, because the retrieved cd isn't up to date
		}
		return cd;
	}

	@Override
	public Long put(Integer cooldownId, Long reuseTimeMillis) {
		if (reuseTimeMillis == null)
			throw new IllegalArgumentException("No cooldown given for ID " + cooldownId);
		return reuseTimeMillis <= System.currentTimeMillis() ? remove(cooldownId) : super.put(cooldownId, reuseTimeMillis);
	}

	/**
	 * @return True if there is a not yet expired cooldown for given ID
	 */
	public boolean hasCooldown(int cooldownId) {
		return containsKey(cooldownId);
	}

	/**
	 * @return Remaining time in seconds when this cooldown expires. 0 if there is no cooldown.
	 */
	public int remainingSeconds(int cooldownId) {
		Long cd = get(cooldownId);
		return cd == null ? 0 : (int) ((cd - System.currentTimeMillis()) / 1000);
	}

}
