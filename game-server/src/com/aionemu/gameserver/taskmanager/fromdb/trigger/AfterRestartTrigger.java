package com.aionemu.gameserver.taskmanager.fromdb.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author nrg
 */
public class AfterRestartTrigger extends TaskFromDBTrigger {

	private static Logger log = LoggerFactory.getLogger(AfterRestartTrigger.class);
	// Indicated wether this task should block or not block the starting progress
	private boolean isBlocking = false;

	@Override
	public boolean isValidTrigger() {
		if (params.length == 1) {
			try {
				isBlocking = Boolean.parseBoolean(this.params[0]);
				return true;
			} catch (Exception e) {
				log.warn("A parameter for AfterRestartTrigger is missing or invalid", e);
			}
		}
		log.warn("Not exact 1 parameter for AfterRestartTrigger received, task is not registered");
		return false;
	}

	@Override
	public void initTrigger() {
		if (!isBlocking) {
			ThreadPoolManager.getInstance().schedule(this, 5000);
		} else {
			this.run();
		}
	}
}
