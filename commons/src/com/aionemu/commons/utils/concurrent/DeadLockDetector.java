package com.aionemu.commons.utils.concurrent;

import java.lang.management.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

/**
 * @author -Nemesiss-, ATracer
 */
public class DeadLockDetector implements Runnable {

	private final Duration checkInterval;
	private final Runnable actionOnDeadlock;
	private final ThreadMXBean tmx;

	private DeadLockDetector(Duration checkInterval, Runnable actionOnDeadlock) {
		this.checkInterval = checkInterval;
		this.actionOnDeadlock = actionOnDeadlock;
		this.tmx = ManagementFactory.getThreadMXBean();
	}

	@Override
	public final void run() {
		while (!detectDeadlock()) {
			try {
				Thread.sleep(checkInterval);
			} catch (InterruptedException ignored) {
				return;
			}
		}
		if (actionOnDeadlock != null)
			actionOnDeadlock.run();
	}

	private boolean detectDeadlock() {
		long[] ids = tmx.findDeadlockedThreads();
		if (ids == null)
			return false;
		ThreadInfo[] tis = tmx.getThreadInfo(ids, true, true);
		Long skippableThreadId = null;
		String info = "Deadlock found:\n";
		for (ThreadInfo ti : tis) {
			if (ti.getLockedSynchronizers().length == 0 && ti.getLockedMonitors().length == 0)
				continue; // this thread is deadlocked but its not guilty
			if (skippableThreadId != null && skippableThreadId == ti.getThreadId()) {
				skippableThreadId = null; // don't log the same relations twice
				continue;
			}

			info += createShortLockInfo(ti);
			ThreadInfo dl = ti;
			while ((dl = tmx.getThreadInfo(new long[]{dl.getLockOwnerId()}, true, true)[0]).getThreadId() != ti.getThreadId()) {
				info += createShortLockInfo(dl);
				if (skippableThreadId == null)
					skippableThreadId = dl.getThreadId();
			}
		}

		info += "\nDeadlocked threads:\n";
		for (ThreadInfo ti : tis)
			info += toStringUnlimited(ti);
		info += "\nRemaining threads:\n";
		Set<Long> ignoredIds = Arrays.stream(ids).boxed().collect(Collectors.toSet());
		for (ThreadInfo ti : tmx.dumpAllThreads(true, true)) {
			if (!ignoredIds.contains(ti.getThreadId()))
				info += toStringUnlimited(ti);
		}
		LoggerFactory.getLogger(DeadLockDetector.class).error(info);
		return true;
	}

	private String createShortLockInfo(ThreadInfo threadInfo) {
		return "\t\"" + threadInfo.getThreadName() + "\" is waiting to lock " + threadInfo.getLockInfo().toString() + " which is held by \""
			+ threadInfo.getLockOwnerName() + "\". " + "Locked synchronizers: " + threadInfo.getLockedSynchronizers().length + ", " + "monitors: "
			+ threadInfo.getLockedMonitors().length + '\n';
	}

	/**
	 * Same as threadInfo.toString() but with full stack trace instead of only the top 8 elements
	 */
	private String toStringUnlimited(ThreadInfo threadInfo) {
		StringBuilder sb = new StringBuilder("\"" + threadInfo.getThreadName() + "\"" + (threadInfo.isDaemon() ? " daemon" : "") + " prio="
			+ threadInfo.getPriority() + " Id=" + threadInfo.getThreadId() + " " + threadInfo.getThreadState());
		if (threadInfo.getLockName() != null)
			sb.append(" on " + threadInfo.getLockName());
		if (threadInfo.getLockOwnerName() != null)
			sb.append(" owned by \"" + threadInfo.getLockOwnerName() + "\" Id=" + threadInfo.getLockOwnerId());
		if (threadInfo.isSuspended())
			sb.append(" (suspended)");
		if (threadInfo.isInNative())
			sb.append(" (in native)");
		sb.append('\n');
		StackTraceElement[] stackTrace = threadInfo.getStackTrace();
		for (int i = 0; i < stackTrace.length; i++) {
			sb.append("\tat " + stackTrace[i].toString() + '\n');
			if (i == 0 && threadInfo.getLockInfo() != null) {
				switch (threadInfo.getThreadState()) {
					case BLOCKED -> sb.append("\t-  blocked on " + threadInfo.getLockInfo() + '\n');
					case WAITING, TIMED_WAITING -> sb.append("\t-  waiting on " + threadInfo.getLockInfo() + '\n');
				}
			}
			for (MonitorInfo mi : threadInfo.getLockedMonitors()) {
				if (mi.getLockedStackDepth() == i) {
					sb.append("\t-  locked " + mi + '\n');
				}
			}
		}
		LockInfo[] locks = threadInfo.getLockedSynchronizers();
		if (locks.length > 0) {
			sb.append("\n\tNumber of locked synchronizers = " + locks.length + '\n');
			for (LockInfo li : locks)
				sb.append("\t- " + li + '\n');
		}
		sb.append('\n');
		return sb.toString();
	}

	public static Thread start(Duration checkInterval, Runnable actionOnDeadlock) {
		return Thread.ofPlatform().name("DeadLockDetector").start(new DeadLockDetector(checkInterval, actionOnDeadlock));
	}
}
