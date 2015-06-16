package com.aionemu.commons.network;

/**
 * DisconnectionThreadPool that will be used to execute DisconnectionTask
 * 
 * @author -Nemesiss-
 */
public interface DisconnectionThreadPool {

	/**
	 * Schedule Disconnection task.
	 * 
	 * @param dt
	 *          <code>DisconnectionTask</code>
	 * @param delay
	 */
	public void scheduleDisconnection(DisconnectionTask dt, long delay);

	/**
	 * Waits till all task end work.
	 */
	public void waitForDisconnectionTasks();
}
