package com.aionemu.commons.utils.concurrent;

import java.lang.management.*;

import org.slf4j.LoggerFactory;

/**
 * @author -Nemesiss-, ATracer
 */
public class DeadLockDetector extends Thread {

	private final int sleepTime, exitOnDeadlock;
	private final ThreadMXBean tmx;

	/**
	 * @param sleepTime - Interval in seconds when deadlock checks are performed
	 * @param exitOnDeadlock - If nonzero, {@code System.exit(exitOnDeadlock)} will be called
	 */
	public DeadLockDetector(int sleepTime, int exitOnDeadlock) {
		super("DeadLockDetector");
		this.sleepTime = sleepTime * 1000;
		this.exitOnDeadlock = exitOnDeadlock;
		this.tmx = ManagementFactory.getThreadMXBean();
		setDaemon(true);
	}

	/**
	 * Check if there is a DeadLock.
	 */
	@Override
	public final void run() {
		while (!detectDeadlock()) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException ignored) {
			}
		}
		if (exitOnDeadlock != 0)
			System.exit(exitOnDeadlock);
	}

	private boolean detectDeadlock() {
		long[] ids = tmx.findDeadlockedThreads();
		if (ids == null)
			return false;
		ThreadInfo[] tis = tmx.getThreadInfo(ids, true, true);
		String info = "DeadLock Found!\n";
		for (ThreadInfo ti : tis)
			info += ti.toString();

		for (ThreadInfo ti : tis) {
			LockInfo[] locks = ti.getLockedSynchronizers();
			MonitorInfo[] monitors = ti.getLockedMonitors();
			if (locks.length == 0 && monitors.length == 0)
				// this thread is deadlocked but its not guilty
				continue;

			ThreadInfo dl = ti;
			info += "Java-level deadlock:\n";
			info += createShortLockInfo(dl);
			while ((dl = tmx.getThreadInfo(new long[]{dl.getLockOwnerId()}, true, true)[0]).getThreadId() != ti.getThreadId())
				info += createShortLockInfo(dl);

			info += "\nDumping all threads:\n";
			for (ThreadInfo dumpedTI : tmx.dumpAllThreads(true, true)) {
				info += printDumpedThreadInfo(dumpedTI);
			}
		}
		LoggerFactory.getLogger(DeadLockDetector.class).warn(info);
		return true;
	}

	/**
	 * Example:
	 * <p>
	 * Java-level deadlock:<br>
	 * Thread-0 is waiting to lock java.lang.Object@276af2 which is held by main. Locked synchronizers:0 monitors:1<br>
	 * main is waiting to lock java.lang.Object@fa3ac1 which is held by Thread-0. Locked synchronizers:0 monitors:1<br>
	 * </p>
	 */
	private String createShortLockInfo(ThreadInfo threadInfo) {
		return "\t" + threadInfo.getThreadName() +
				" is waiting to lock " + threadInfo.getLockInfo().toString() +
				" which is held by " + threadInfo.getLockOwnerName() + ". " +
				"Locked synchronizers: " + threadInfo.getLockedSynchronizers().length + ", " +
				"monitors: " + threadInfo.getLockedMonitors().length +
				"\n";
	}

	/**
	 * Full thread info (short info and stacktrace)<br>
	 * Example:
	 * <p>
	 * "Thread-0" Id=10 BLOCKED <br>
	 * at com.aionemu.gameserver.DeadlockTest$1$1.run(DeadlockTest.java:70)<br>
	 * - locked java.lang.Object@fa3ac1<br>
	 * at java.lang.Thread.run(Thread.java:662)
	 * </p>
	 */
	private String printDumpedThreadInfo(ThreadInfo threadInfo) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n\"" + threadInfo.getThreadName() + "\"" + " Id=" + threadInfo.getThreadId() + " " + threadInfo.getThreadState() + "\n");
		StackTraceElement[] stacktrace = threadInfo.getStackTrace();
		for (int i = 0; i < stacktrace.length; i++) {
			StackTraceElement ste = stacktrace[i];
			sb.append("\t" + "at " + ste.toString() + "\n");
			for (MonitorInfo mi : threadInfo.getLockedMonitors()) {
				if (mi.getLockedStackDepth() == i) {
					sb.append("\t-  locked " + mi);
					sb.append('\n');
				}
			}
		}
		return sb.toString();
	}
}
