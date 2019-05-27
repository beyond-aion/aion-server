package com.aionemu.gameserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.clientpackets.CM_PING;
import com.aionemu.gameserver.network.aion.clientpackets.CM_PING_INGAME;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer
 */
public class DebugService {

	private static final Logger log = LoggerFactory.getLogger(DebugService.class);

	private static final int ANALYZE_PLAYERS_INTERVAL = 30 * 60 * 1000;

	public static DebugService getInstance() {
		return SingletonHolder.instance;
	}

	private DebugService() {
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this::analyzeWorldPlayers, ANALYZE_PLAYERS_INTERVAL, ANALYZE_PLAYERS_INTERVAL);
		log.info("DebugService started. Analyze interval: " + ANALYZE_PLAYERS_INTERVAL);
	}

	private void analyzeWorldPlayers() {
		log.info("Starting analysis of world players");

		for (Player player : World.getInstance().getAllPlayers()) {
			// Check connection
			AionConnection connection = player.getClientConnection();
			if (connection == null) {
				log.warn("[DEBUG SERVICE] Found {} without connection: Spawned {}", player, player.isSpawned());
				continue;
			}

			long lastAlternativePing = Math.min(connection.getLastPingTime(false), connection.getLastPingTime(true));
			long lastPing = Math.max(connection.getLastPingTime(false), connection.getLastPingTime(true));
			if (lastPing > 0) {
				long pingInterval = System.currentTimeMillis() - lastPing;
				if (pingInterval - 5000 > CM_PING_INGAME.CLIENT_PING_INTERVAL)
					log.warn("[DEBUG SERVICE] Found {} with large ping interval: Spawned {}, PingMS {}", player, player.isSpawned(), pingInterval);
				if (lastAlternativePing > 0 && lastPing - lastAlternativePing > CM_PING.CLIENT_PING_INTERVAL)
					AuditLogger.log(player, "has conspicuous ping pattern, may be using speedhack or anti speedhack detection of some sort.");
			}
		}

		log.info("Analysis of world players finished");
	}

	private static class SingletonHolder {

		protected static final DebugService instance = new DebugService();
	}
}
