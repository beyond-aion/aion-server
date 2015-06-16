package com.aionemu.commons.services.cron;

public class CurrentThreadRunnableRunner extends RunnableRunner {

	@Override
	public void executeRunnable(Runnable r) {
		r.run();
	}

	@Override
	public void executeLongRunningRunnable(Runnable r) {
		executeRunnable(r);
	}
}
