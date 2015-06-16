package com.aionemu.gameserver.model.items;

/**
 * @author ATracer
 */
public class ItemCooldown {

	/**
	 * time of next reuse
	 */
	private long time;
	/**
	 * Use delay in ms
	 */
	private int useDelay;

	/**
	 * @param time
	 * @param useDelay
	 */
	public ItemCooldown(long time, int useDelay) {
		this.time = time;
		this.useDelay = useDelay;
	}

	/**
	 * @return the time
	 */
	public long getReuseTime() {
		return time;
	}

	/**
	 * @return the useDelay
	 */
	public int getUseDelay() {
		return useDelay;
	}
}
