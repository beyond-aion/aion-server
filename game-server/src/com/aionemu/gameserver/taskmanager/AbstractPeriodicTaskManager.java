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
		log.info(getClass().getSimpleName() + " initialized.");
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, Rnd.get(500, 550), period);
	}
}
