package com.aionemu.gameserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer
 */
public class DebugService {

	private static final Logger log = LoggerFactory.getLogger(DebugService.class);

	private static final int ANALYZE_PLAYERS_INTERVAL = 30 * 60 * 1000;

	public static final DebugService getInstance() {
		return SingletonHolder.instance;
	}

	private DebugService() {
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				analyzeWorldPlayers();
			}

		}, ANALYZE_PLAYERS_INTERVAL, ANALYZE_PLAYERS_INTERVAL);
		log.info("DebugService started. Analyze iterval: " + ANALYZE_PLAYERS_INTERVAL);
	}

	private void analyzeWorldPlayers() {
		log.info("Starting analysis of world players");

		for (Player player : World.getInstance().getAllPlayers()) {
			/**
			 * Check connection
			 */
			AionConnection connection = player.getClientConnection();
			if (connection == null) {
				log.warn("[DEBUG SERVICE] Found {} without connection: Spawned {}", player, player.isSpawned());
				continue;
			}

			/**
			 * Check CM_PING packet
			 */
			long lastPingTimeMS = connection.getLastPingTime();
			long pingInterval = System.currentTimeMillis() - lastPingTimeMS;
			if (lastPingTimeMS > 0 && pingInterval > 300000) {
				if (pingInterval > 600000) { // close connection if client didn't send any ping for at least 10 minutes
					connection.close();
					log.warn("[DEBUG SERVICE] Kicking {} with large ping interval: Spawned {}, PingMS {}", player, player.isSpawned(), pingInterval);
				} else {
					log.warn("[DEBUG SERVICE] Found {} with large ping interval: Spawned {}, PingMS {}", player, player.isSpawned(), pingInterval);
				}
			}
		}

		log.info("Analysis of world players finished");
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		protected static final DebugService instance = new DebugService();
	}
}
