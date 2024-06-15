package com.aionemu.gameserver.taskmanager;

import java.util.Date;
import java.util.concurrent.Semaphore;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.dao.ServerVariablesDAO;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rolandas, Neon
 */
public abstract class AbstractCronTask implements Runnable {

	protected static final Long SERVER_STOP_MILLIS = ServerVariablesDAO.loadLong("serverLastRun");
	private static final Semaphore semaphore = new Semaphore(1);
	protected final Logger log = LoggerFactory.getLogger(getClass());
	private final CronExpression cronExpression;
	private Date lastPlannedRunBeforeServerStart;
	private Date lastRun;
	private Date nextRun;

	public AbstractCronTask(CronExpression cronExpression) {
		this.cronExpression = cronExpression;
		if (this.cronExpression == null) {
			log.info(getClass().getSimpleName() + " is deactivated");
			return;
		}
		this.nextRun = getNextRunAfter(new Date());
		this.lastPlannedRunBeforeServerStart = findLastPlannedRun();
		runAndScheduleAsyncWithLock();
	}

	/**
	 * Runs this task in another thread, so that constructor can finish and external references to this task don't throw null pointer exceptions.
	 * This additionally makes sure that other tasks don't run before a previous one is finished, so multiple tasks are initialized semi-synchronous.
	 */
	private void runAndScheduleAsyncWithLock() {
		semaphore.acquireUninterruptibly();
		ThreadPoolManager.getInstance().executeLongRunning(() -> {
			if (shouldRunOnStart())
				run();
			CronService.getInstance().schedule(this, cronExpression, true);
			log.info("Scheduled " + getClass().getSimpleName() + " with cron expression: " + cronExpression);
			semaphore.release();
		});
	}

	/**
	 * @return Default implementation returns true if the server was down when task should have run
	 */
	protected boolean shouldRunOnStart() {
		return SERVER_STOP_MILLIS != null && lastPlannedRunBeforeServerStart != null && SERVER_STOP_MILLIS < lastPlannedRunBeforeServerStart.getTime();
	}

	/**
	 * @return The last time this task started, null if it didn't during this uptime yet
	 */
	public final Date getLastRun() {
		return lastRun;
	}

	/**
	 * @return The last time this task started or should have started (in case task hasn't yet run since the server got restarted)
	 */
	public final Date getLastPlannedRun() {
		return lastRun == null ? lastPlannedRunBeforeServerStart : lastRun;
	}

	public final long getMillisSinceLastRun() {
		return lastRun == null ? -1 : System.currentTimeMillis() - lastRun.getTime();
	}

	/**
	 * @return Time of the next task start
	 */
	public final Date getNextRun() {
		return nextRun;
	}

	/**
	 * @return Time of the next task start after given date
	 */
	public final Date getNextRunAfter(Date date) {
		return cronExpression.getTimeAfter(date);
	}

	public final long getMillisUntilNextRun() {
		return nextRun.getTime() - System.currentTimeMillis();
	}

	protected abstract void executeTask();

	@Override
	public final void run() {
		lastRun = new Date();
		nextRun = getNextRunAfter(lastRun);
		executeTask();
	}

	/**
	 * @return Date when this task last should have run, whether the server was online or not. <b>NOTE</b>: The current implementation may not find the
	 *         correct date if the underlying cron expression is irregular
	 */
	private Date findLastPlannedRun() {
		long interval = getNextRunAfter(nextRun).getTime() - nextRun.getTime();
		long now = System.currentTimeMillis();
		long millis = now;
		Date lastRun;
		do {
			millis -= interval / 2;
			lastRun = cronExpression.getTimeAfter(new Date(millis));
		} while (lastRun.getTime() >= now);
		return lastRun;
	}
}
