package com.aionemu.loginserver.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.configs.Config;
import com.aionemu.loginserver.network.aion.clientpackets.CM_LOGIN;

/**
 * @author Mr. Poke
 */
public class FloodProtector {

	private static final Logger log = LoggerFactory.getLogger(CM_LOGIN.class);
	private static final Map<String, FloodInfo> flooders = new ConcurrentHashMap<>();

	public static boolean tooFast(String ip) {
		if (Config.FLOODPROTECTOR_EXCLUDED_IPS.contains(ip))
			return false;
		return flooders.compute(ip, (k, floodInfo) -> {
			if (floodInfo == null) {
				floodInfo = new FloodInfo();
			} else if (!floodInfo.isBanned() && floodInfo.lastAllowedRequestMillis + Config.FAST_RECONNECTION_TIME * 1000 > System.currentTimeMillis()) {
				log.info("Blocked " + ip + " for " + Config.WRONG_LOGIN_BAN_TIME + " minutes (reconnected too fast)");
				floodInfo.banExpirationMillis = System.currentTimeMillis() + Config.WRONG_LOGIN_BAN_TIME * 60000;
			}
			if (!floodInfo.isBanned())
				floodInfo.lastAllowedRequestMillis = System.currentTimeMillis();
			return floodInfo;
		}).isBanned();
	}

	private static class FloodInfo {
		private long banExpirationMillis, lastAllowedRequestMillis;

		private boolean isBanned() {
			return banExpirationMillis > System.currentTimeMillis();
		}
	}
}
