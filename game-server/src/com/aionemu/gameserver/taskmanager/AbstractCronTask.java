package com.aionemu.gameserver.taskmanager;

import java.util.Date;

import org.quartz.CronExpression;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.dao.ServerVariablesDAO;

/**
 * @author Rolandas, Neon
 */
public abstract class AbstractCronTask implements Runnable {

	private static final Long SERVER_STOP_MILLIS = DAOManager.getDAO(ServerVariablesDAO.class).loadLong("serverLastRun");
	private final CronExpression cronExpression;
	private Date lastPlannedRunBeforeServerStart;
	private Date lastRun;
	private Date nextRun;

	public AbstractCronTask(CronExpression cronExpression) {
		this.cronExpression = cronExpression;
		this.nextRun = getNextRunAfter(new Date());
		this.lastPlannedRunBeforeServerStart = findLastPlannedRun();
		if (SERVER_STOP_MILLIS != null && lastPlannedRunBeforeServerStart != null && SERVER_STOP_MILLIS < lastPlannedRunBeforeServerStart.getTime())
			run(); // server was down when task should have run, so we run it now
		CronService.getInstance().schedule(this, cronExpression, true);
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
