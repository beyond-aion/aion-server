package com.aionemu.gameserver.services;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.dao.ServerVariablesDAO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GAME_TIME;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.time.gametime.GameTime;

/**
 * @author ATracer, Neon
 */
public class GameTimeService {

	private static final Logger log = LoggerFactory.getLogger(GameTimeService.class);
	private final GameTime gameTime = new GameTime(ServerVariablesDAO.loadInt("time"));
	private final AtomicBoolean isStarted = new AtomicBoolean();

	private GameTimeService() {
		log.info("Initialized GameTime");
	}

	/**
	 * @return The current {@link GameTime}.
	 */
	public GameTime getGameTime() {
		return gameTime;
	}

	/**
	 * Saves the current time to the database
	 *
	 * @return True on success.
	 */
	public boolean saveGameTime() {
		return ServerVariablesDAO.store("time", gameTime.getTime());
	}

	public void startClock() {
		if (!isStarted.compareAndSet(false, true))
			throw new GameServerError("Tried to start game time twice.");

		int updateInterval = 3 * 60000; // every 3 minutes

		// task to increase the game time every 5 seconds by a minute
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> gameTime.addMinutes(1), 5000, 5000);

		// task to save the game time and update all clients
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			log.info("Sending current game time to all players");
			PacketSendUtility.broadcastToWorld(new SM_GAME_TIME());
			if (saveGameTime())
				log.info("Game time saved...");
			else
				log.warn("Error saving game time");
		}, updateInterval, updateInterval);

		log.info("GameTime started. Update interval: " + updateInterval / 1000 + "s");
	}

	public static final GameTimeService getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final GameTimeService instance = new GameTimeService();
	}
}
