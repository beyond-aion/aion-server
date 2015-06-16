package com.aionemu.gameserver.utils.cron;

import com.aionemu.commons.services.cron.RunnableRunner;
import com.aionemu.gameserver.utils.ThreadPoolManager;

public class ThreadPoolManagerRunnableRunner extends RunnableRunner{

	@Override
	public void executeRunnable(Runnable r) {
		ThreadPoolManager.getInstance().execute(r);
	}

	@Override
	public void executeLongRunningRunnable(Runnable r) {
		ThreadPoolManager.getInstance().executeLongRunning(r);
	}
}
