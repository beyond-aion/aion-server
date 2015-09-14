package com.aionemu.commons.callbacks;

/**
 * Interface that is used to mark callback priority when it's not default.<br>
 * Callback doesn't have to implement this interface if priority is default.<br>
 * Listeners with bigger priority are executed earlier.
 *
 * @author SoulKeeper
 */
public interface CallbackPriority {

	/**
	 * Returns default priority of callback
	 */
	public static final int DEFAULT_PRIORITY = 0;

	/**
	 * Returns callbacks priority
	 *
	 * @return priority of callback
	 */
	public int getPriority();
}
