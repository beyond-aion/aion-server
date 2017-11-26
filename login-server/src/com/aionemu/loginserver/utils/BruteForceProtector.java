package com.aionemu.loginserver.utils;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.loginserver.configs.Config;

/**
 * @author Mr. Poke
 */
public class BruteForceProtector {

	private Map<String, FailedLoginInfo> failedConnections = new HashMap<>();

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
		FailedLoginInfo failed = failedConnections.get(ip);
		if (failed == null || System.currentTimeMillis() - failed.getTime() > Config.WRONG_LOGIN_BAN_TIME * 1000 * 60) {
			failedConnections.put(ip, new FailedLoginInfo(1, System.currentTimeMillis()));
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
