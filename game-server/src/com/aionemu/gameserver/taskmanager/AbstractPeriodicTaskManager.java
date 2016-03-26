package com.aionemu.gameserver.taskmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.taskmanager.AbstractLockManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author lord_rex and MrPoke based on l2j-free engines. This can be used for periodic calls.
 */
public abstract class AbstractPeriodicTaskManager extends AbstractLockManager implements Runnable {

	protected static final Logger log = LoggerFactory.getLogger(AbstractPeriodicTaskManager.class);

	public AbstractPeriodicTaskManager(int period) {
		int delay = Math.max(0, Rnd.get(period - 5, period + 5));
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, delay, delay);
		log.info(getClass().getSimpleName() + " initialized.");
	}
}
