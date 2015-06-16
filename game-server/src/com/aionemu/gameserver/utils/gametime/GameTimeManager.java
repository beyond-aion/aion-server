package com.aionemu.gameserver.utils.gametime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.ServerVariablesDAO;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * Manages ingame time
 * 
 * @author Ben
 */
public class GameTimeManager {

	private static final Logger log = LoggerFactory.getLogger(GameTimeManager.class);
	private static GameTime instance;
	private static GameTimeUpdater updater;
	private static boolean clockStarted = false;

	static {
		ServerVariablesDAO dao = DAOManager.getDAO(ServerVariablesDAO.class);
		instance = new GameTime(dao.load("time"));
	}

	/**
	 * Gets the current GameTime
	 * 
	 * @return GameTime
	 */
	public static GameTime getGameTime() {
		return instance;
	}

	/**
	 * Starts the counter that increases the clock every tick
	 * 
	 * @throws IllegalStateException
	 *           If called twice
	 */
	public static void startClock() {
		if (clockStarted) {
			throw new IllegalStateException("Clock is already started");
		}

		updater = new GameTimeUpdater(getGameTime());
		ThreadPoolManager.getInstance().scheduleAtFixedRate(updater, 0, 5000);

		clockStarted = true;
	}

	/**
	 * Saves the current time to the database
	 * 
	 * @return Success
	 */
	public static boolean saveTime() {
		log.info("Game time saved...");
		return DAOManager.getDAO(ServerVariablesDAO.class).store("time",getGameTime().getTime());
	}

	/**
	 * Clean scheduled queues, set a new GameTime, then restart the clock
	 */
	public static void reloadTime(int time) {
		ThreadPoolManager.getInstance().purge();
		instance = new GameTime(time);

		clockStarted = false;

		startClock();
		log.info("Game time changed by admin and clock restarted...");
	}
}
