package com.aionemu.loginserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.GameServerInfo;
import com.aionemu.loginserver.GameServerTable;
import com.aionemu.loginserver.dao.PremiumDAO;
import com.aionemu.loginserver.network.gameserver.serverpackets.SM_PREMIUM_RESPONSE;

/**
 * @author KID
 */
public class PremiumController {

	private static final Logger log = LoggerFactory.getLogger("PREMIUM_CTRL");
	private static final int RESULT_FAIL = 1;
	private static final int RESULT_LOW_POINTS = 2;
	private static final int RESULT_OK = 3;
	private static final int RESULT_ADD = 4;

	private PremiumController() {
	}

	public static void requestBuy(int accountId, int requestId, long cost, byte serverId) {
		long points = PremiumDAO.getPoints(accountId);

		GameServerInfo server = GameServerTable.getGameServerInfo(serverId);
		if (server == null || server.getConnection() == null || !server.isAccountOnGameServer(accountId)) {
			log.error("Account " + accountId + " requested " + requestId + " from gs #" + serverId + " and server is down.");
			return;
		}

		// adding new tolls
		if (cost < 0) {
			long ncnt = points + (cost * -1);
			PremiumDAO.updatePoints(accountId, ncnt, 0);
			server.getConnection().sendPacket(new SM_PREMIUM_RESPONSE(requestId, RESULT_ADD, ncnt));
			return;
		}

		if (points < cost) {
			server.getConnection().sendPacket(new SM_PREMIUM_RESPONSE(requestId, RESULT_LOW_POINTS, points));
			return;
		}

		if (PremiumDAO.updatePoints(accountId, points, cost)) {
			points -= cost;
			server.getConnection().sendPacket(new SM_PREMIUM_RESPONSE(requestId, RESULT_OK, points));
			log.info("Account " + accountId + " purchased lot #" + requestId + " for " + cost + " from server #" + serverId);
		} else {
			server.getConnection().sendPacket(new SM_PREMIUM_RESPONSE(requestId, RESULT_FAIL, points));
			log.info("Account " + accountId + " failed in purchasing lot #" + requestId + " for " + cost + " from server #" + serverId);
		}
	}
}
