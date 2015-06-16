package com.aionemu.commons.services.cron;

import org.quartz.*;

public abstract class RunnableRunner implements Job {

	public static final String KEY_RUNNABLE_OBJECT = "cronservice.scheduled.runnable.instance";
	public static final String KEY_PROPERTY_IS_LONGRUNNING_TASK  = "cronservice.scheduled.runnable.islognrunning";
	public static final String KEY_CRON_EXPRESSION = "cronservice.scheduled.runnable.cronexpression";

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		JobDataMap jdm = context.getJobDetail().getJobDataMap();

		Runnable r = (Runnable)jdm.get(KEY_RUNNABLE_OBJECT);
		boolean longRunning = jdm.getBoolean(KEY_PROPERTY_IS_LONGRUNNING_TASK);

		if(longRunning){
			executeLongRunningRunnable(r);
		} else {
			executeRunnable(r);
		}
	}

	public abstract void executeRunnable(Runnable r);

	public abstract void executeLongRunningRunnable(Runnable r);
}
