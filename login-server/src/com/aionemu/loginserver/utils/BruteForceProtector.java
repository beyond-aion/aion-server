package com.aionemu.loginserver.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.loginserver.configs.Config;

/**
 * @author Mr. Poke
 */
public class BruteForceProtector {

	private static final int PURGE_THRESHOLD = 1024;

	private final Map<String, FailedLoginInfo> failedConnections = new ConcurrentHashMap<>();

	class FailedLoginInfo {

		private int count;
		private long time;

		/**
		 * @param count
		 * @param time
		 */
		public FailedLoginInfo(int count, long time) {
			this.count = count;
			this.time = time;
		}

		public void increaseCount() {
			count++;
		}

		/**
		 * @return the count
		 */
		public int getCount() {
			return count;
		}

		/**
		 * @return the time
		 */
		public long getTime() {
			return time;
		}
	}

	public static final BruteForceProtector getInstance() {
		return SingletonHolder.instance;
	}

	public boolean addFailedConnect(String ip) {
		long now = System.currentTimeMillis();
		long windowMillis = Config.WRONG_LOGIN_BAN_TIME * 60_000L;
		FailedLoginInfo failed = failedConnections.get(ip);
		if (failed == null || now - failed.getTime() > windowMillis) {
			if (failedConnections.size() > PURGE_THRESHOLD) // the map only grows: drop the entries that can no longer be banned
				failedConnections.values().removeIf(info -> now - info.getTime() > windowMillis);
			failedConnections.put(ip, new FailedLoginInfo(1, now));
		} else {
			if (failed.getCount() >= Config.LOGIN_TRY_BEFORE_BAN) {
				failedConnections.remove(ip);
				return true;
			} else
				failed.increaseCount();
		}
		return false;
	}

	private static class SingletonHolder {

		protected static final BruteForceProtector instance = new BruteForceProtector();
	}
}
