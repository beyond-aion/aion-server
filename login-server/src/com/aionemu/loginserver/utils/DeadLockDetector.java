package com.aionemu.loginserver.utils;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.ExitCode;

/**
 * @author -Nemesiss-
 */
public class DeadLockDetector extends Thread {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(DeadLockDetector.class);
	/** What should we do on DeadLock */
	public static final byte NOTHING = 0;
	/** What should we do on DeadLock */
	public static final byte RESTART = 1;

	/** how often check for deadlocks */
	private final int sleepTime;
	/**
	 * ThreadMXBean
	 */
	private final ThreadMXBean tmx;
	/** What should we do on DeadLock */
	private final byte doWhenDL;

	/**
	 * Create new DeadLockDetector with given values.
	 * 
	 * @param sleepTime
	 * @param doWhenDL
	 */
	public DeadLockDetector(int sleepTime, byte doWhenDL) {
		super("DeadLockDetector");
		this.sleepTime = sleepTime * 1000;
		this.tmx = ManagementFactory.getThreadMXBean();
		this.doWhenDL = doWhenDL;
	}

	/**
	 * Check if there is a DeadLock.
	 */
	@Override
	public final void run() {
		boolean deadlock = false;
		while (!deadlock) {
			try {
				long[] ids = tmx.findDeadlockedThreads();

				if (ids != null) {
					/** deadlock found :/ */
					deadlock = true;
					ThreadInfo[] tis = tmx.getThreadInfo(ids, true, true);
					String info = "DeadLock Found!\n";
					for (ThreadInfo ti : tis) {
						info += ti.toString();
					}

					for (ThreadInfo ti : tis) {
						LockInfo[] locks = ti.getLockedSynchronizers();
						MonitorInfo[] monitors = ti.getLockedMonitors();
						if (locks.length == 0 && monitors.length == 0) {
							/** this thread is deadlocked but its not guilty */
							continue;
						}

						ThreadInfo dl = ti;
						info += "Java-level deadlock:\n";
						info += "\t" + dl.getThreadName() + " is waiting to lock " + dl.getLockInfo().toString()
							+ " which is held by " + dl.getLockOwnerName() + "\n";
						while ((dl = tmx.getThreadInfo(new long[] { dl.getLockOwnerId() }, true, true)[0]).getThreadId() != ti
							.getThreadId()) {
							info += "\t" + dl.getThreadName() + " is waiting to lock " + dl.getLockInfo().toString()
								+ " which is held by " + dl.getLockOwnerName() + "\n";
						}
					}
					log.warn(info);

					if (doWhenDL == RESTART) {
						System.exit(ExitCode.CODE_RESTART);
					}
				}
				Thread.sleep(sleepTime);
			}
			catch (Exception e) {
				log.warn("DeadLockDetector: " + e, e);
			}
		}
	}
}
