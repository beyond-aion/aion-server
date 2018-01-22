package com.aionemu.commons.services;

import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.services.cron.CronServiceException;
import com.aionemu.commons.services.cron.RunnableRunner;
import com.aionemu.commons.utils.GenericValidator;

/**
 * @author SoulKeeper
 * @modified Neon
 */
public final class CronService {

	private static final Logger log = LoggerFactory.getLogger(CronService.class);

	private static CronService instance;

	private final TimeZone timeZone;
	private final Scheduler scheduler;
	private final Class<? extends RunnableRunner> runnableRunner;

	public static CronService getInstance() {
		return instance;
	}

	public static synchronized void initSingleton(Class<? extends RunnableRunner> runnableRunner, TimeZone timeZone) {
		if (instance != null) {
			throw new CronServiceException("CronService is already initialized");
		}

		instance = new CronService(runnableRunner, timeZone);
	}

	/**
	 * Private constructor to prevent instantiation.<br>
	 * Can be instantiated using reflection (for tests), but no real use for application please!
	 */
	private CronService(Class<? extends RunnableRunner> runnableRunner, TimeZone timeZone) {
		Properties properties = new Properties();
		properties.setProperty("org.quartz.threadPool.threadCount", "1");

		try {
			scheduler = new StdSchedulerFactory(properties).getScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			throw new CronServiceException("Failed to initialize CronService", e);
		}
		if (runnableRunner == null) {
			throw new CronServiceException("RunnableRunner class must be defined");
		}

		this.runnableRunner = runnableRunner;
		this.timeZone = timeZone;
	}

	public void shutdown() {
		try {
			scheduler.shutdown(false);
		} catch (SchedulerException e) {
			log.error("Failed to shutdown CronService correctly", e);
		}
	}

	public JobDetail schedule(Runnable r, String cronExpression) {
		return schedule(r, cronExpression, false);
	}

	public JobDetail schedule(Runnable r, String cronExpression, boolean longRunning) {
		try {
			return schedule(r, new CronExpression(cronExpression), longRunning);
		} catch (ParseException e) {
			throw new RuntimeException("CronExpression \"" + cronExpression + "\" is invalid.", e);
		}
	}

	public JobDetail schedule(Runnable r, CronExpression cronExpression) {
		return schedule(r, cronExpression, false);
	}

	public JobDetail schedule(Runnable r, CronExpression cronExpression, boolean longRunning) {
		return schedule(r, runnableRunner, cronExpression, longRunning);
	}

	public JobDetail schedule(Runnable r, Class<? extends RunnableRunner> runnableRunner, CronExpression cronExpression, boolean longRunning) {
		try {
			JobDataMap jdm = new JobDataMap();
			jdm.put(RunnableRunner.KEY_RUNNABLE_OBJECT, r);
			jdm.put(RunnableRunner.KEY_PROPERTY_IS_LONGRUNNING_TASK, longRunning);
			jdm.put(RunnableRunner.KEY_CRON_EXPRESSION, cronExpression);

			String jobId = "Started at ms" + System.currentTimeMillis() + "; ns" + System.nanoTime();
			JobKey jobKey = new JobKey("JobKey:" + jobId);
			JobDetail jobDetail = JobBuilder.newJob(runnableRunner).usingJobData(jdm).withIdentity(jobKey).build();

			CronScheduleBuilder csb = CronScheduleBuilder.cronSchedule(cronExpression).inTimeZone(timeZone);
			CronTrigger trigger = TriggerBuilder.newTrigger().withSchedule(csb).build();

			scheduler.scheduleJob(jobDetail, trigger);
			return jobDetail;
		} catch (Exception e) {
			throw new CronServiceException("Failed to start job", e);
		}
	}

	public boolean cancel(Runnable r) {
		Map<Runnable, JobDetail> map = getRunnables();
		JobDetail jd = map.get(r);
		return cancel(jd);
	}

	public boolean cancel(JobDetail jd) {
		if (jd == null) {
			return false;
		}

		if (jd.getKey() == null) {
			throw new CronServiceException("JobDetail should have JobKey");
		}

		try {
			return scheduler.deleteJob(jd.getKey());
		} catch (SchedulerException e) {
			throw new CronServiceException("Failed to delete Job", e);
		}
	}

	protected Collection<JobDetail> getJobDetails() {
		if (scheduler == null) {
			return Collections.emptySet();
		}

		try {
			Set<JobKey> keys = scheduler.getJobKeys(null);

			if (GenericValidator.isBlankOrNull(keys)) {
				return Collections.emptySet();
			}

			Set<JobDetail> result = new HashSet<>(keys.size());
			for (JobKey jk : keys) {
				result.add(scheduler.getJobDetail(jk));
			}

			return result;
		} catch (Exception e) {
			throw new CronServiceException("Can't get all active job details", e);
		}
	}

	public Map<Runnable, JobDetail> getRunnables() {
		Collection<JobDetail> jobDetails = getJobDetails();
		if (GenericValidator.isBlankOrNull(jobDetails)) {
			return Collections.emptyMap();
		}

		Map<Runnable, JobDetail> result = new HashMap<>();
		for (JobDetail jd : jobDetails) {
			if (GenericValidator.isBlankOrNull(jd.getJobDataMap())) {
				continue;
			}

			if (jd.getJobDataMap().containsKey(RunnableRunner.KEY_RUNNABLE_OBJECT)) {
				result.put((Runnable) jd.getJobDataMap().get(RunnableRunner.KEY_RUNNABLE_OBJECT), jd);
			}
		}

		return Collections.unmodifiableMap(result);
	}

	public List<? extends Trigger> getJobTriggers(JobDetail jd) {
		return getJobTriggers(jd.getKey());
	}

	public List<? extends Trigger> getJobTriggers(JobKey jk) {
		if (scheduler == null) {
			return Collections.emptyList();
		}

		try {
			return scheduler.getTriggersOfJob(jk);
		} catch (SchedulerException e) {
			throw new CronServiceException("Can't get triggers for JobKey " + jk, e);
		}
	}
}
