package com.aionemu.gameserver.services;

import java.util.Iterator;

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

		Iterator<Player> playersIterator = World.getInstance().getPlayersIterator();
		while (playersIterator.hasNext()) {
			Player player = playersIterator.next();

			/**
			 * Check connection
			 */
			AionConnection connection = player.getClientConnection();
			if (connection == null) {
				log.warn(String.format("[DEBUG SERVICE] Player without connection: " + "detected: ObjId %d, Name %s, Spawned %s", player.getObjectId(),
					player.getName(), player.isSpawned()));
				continue;
			}

			/**
			 * Check CM_PING packet
			 */
			long lastPingTimeMS = connection.getLastPingTime();
			long pingInterval = System.currentTimeMillis() - lastPingTimeMS;
			if (lastPingTimeMS > 0 && pingInterval > 300000) {
				log.warn(String.format("[DEBUG SERVICE] Player with large ping interval: " + "ObjId %d, Name %s, Spawned %s, PingMS %d",
					player.getObjectId(), player.getName(), player.isSpawned(), pingInterval));
			}
		}

		log.info("Analysis of world players finished");
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		protected static final DebugService instance = new DebugService();
	}
}
