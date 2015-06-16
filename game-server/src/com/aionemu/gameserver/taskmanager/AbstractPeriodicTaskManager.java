package com.aionemu.gameserver.taskmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.taskmanager.AbstractLockManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.GameServer.StartupHook;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author lord_rex and MrPoke based on l2j-free engines. This can be used for periodic calls.
 */
public abstract class AbstractPeriodicTaskManager extends AbstractLockManager implements Runnable, StartupHook {

	protected static final Logger log = LoggerFactory.getLogger(AbstractPeriodicTaskManager.class);

	private final int period;

	public AbstractPeriodicTaskManager(int period) {
		this.period = period;

		GameServer.addStartupHook(this);

		log.info(getClass().getSimpleName() + ": Initialized.");
	}

	@Override
	public final void onStartup() {
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000 + Rnd.get(period), Rnd.get(period - 5, period + 5));
	}

	@Override
	public abstract void run();
}
