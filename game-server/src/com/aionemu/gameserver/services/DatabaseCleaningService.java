package com.aionemu.gameserver.services;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javolution.util.FastTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.CleaningConfig;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * Offers the functionality to delete all data about inactive players
 *
 * @author nrg
 */
public class DatabaseCleaningService {

	private Logger log = LoggerFactory.getLogger(DatabaseCleaningService.class);
	private PlayerDAO dao = DAOManager.getDAO(PlayerDAO.class);
	// A limit of cleaning period for security reasons
	private final int SECURITY_MINIMUM_PERIOD = 30;
	// Worker execution check time
	private final int WORKER_CHECK_TIME = 10 * 1000;
	// Singelton
	private static DatabaseCleaningService instance = new DatabaseCleaningService();
	// Workers
	private List<Worker> workers;
	// Starttime of service
	private long startTime;

	/**
	 * Private constructor to avoid extern access and ensure singelton
	 */
	private DatabaseCleaningService() {
		if (CleaningConfig.CLEANING_ENABLE) {
			runCleaning();
		}
	}

	/**
	 * Cleans the databse from inactive player data
	 */
	private void runCleaning() {
		// Execution time
		log.info("DatabaseCleaningService: Executing database cleaning");
		startTime = System.currentTimeMillis();

		// getting period for deletion
		int periodInDays = CleaningConfig.CLEANING_PERIOD;

		// only a security feature
		if (periodInDays > SECURITY_MINIMUM_PERIOD) {
			delegateToThreads(CleaningConfig.CLEANING_THREADS, dao.getInactiveAccounts(periodInDays));
			monitoringProcess();
		} else {
			log.warn("The configured days for database cleaning is too low. For security reasons the service will only execute with periods over 30 days!");
		}
	}

	private void monitoringProcess() {
		while (!allWorkersReady()) {
			try {
				Thread.sleep(WORKER_CHECK_TIME);
				log.info("DatabaseCleaningService: Until now " + currentlyDeletedChars() + " chars deleted in " + (System.currentTimeMillis() - startTime)
					/ 1000 + " seconds!");
			} catch (InterruptedException ex) {
				log.error("DatabaseCleaningService: Got Interrupted!");
			}
		}
		log.info("DatabaseCleaningService: Cleaning finished! Deleted " + currentlyDeletedChars() + " chars in "
			+ (System.currentTimeMillis() - startTime) / 1000 + " seconds!");
	}

	private boolean allWorkersReady() {
		for (Worker w : workers) {
			if (!w._READY) {
				return false;
			}
		}
		return true;
	}

	private int currentlyDeletedChars() {
		int deletedChars = 0;
		for (Worker w : workers) {
			deletedChars += w.deletedChars;
		}
		return deletedChars;
	}

	private void delegateToThreads(int numberOfThreads, Set<Integer> idsToDelegate) {
		workers = new FastTable<>();
		log.info("DatabaseCleaningService: Executing deletion over " + numberOfThreads + " longrunning threads");

		// every id to another worker with maximum of n different workers
		Iterator<Integer> i = idsToDelegate.iterator();
		int toDelete = 0;
		int limit = CleaningConfig.CLEANING_LIMIT;
		for (int workerNo = 0; i.hasNext(); workerNo = ++workerNo % numberOfThreads) {
			if (workerNo >= workers.size()) {
				workers.add(new Worker());
			}
			workers.get(workerNo).ids.add(i.next());
			if (++toDelete >= limit)
				break;
		}

		// get them working on our longrunning
		for (Worker w : workers) {
			ThreadPoolManager.getInstance().executeLongRunning(w);
		}
	}

	/**
	 * The only extern access
	 *
	 * @return a singleton DatabaseCleaningService
	 */
	public static DatabaseCleaningService getInstance() {
		return instance;
	}

	private class Worker implements Runnable {

		private List<Integer> ids = new FastTable<Integer>();
		private int deletedChars = 0;
		private boolean _READY = false;

		@Override
		public void run() {
			for (int id : ids) {
				deletedChars += PlayerService.deleteAccountsCharsFromDB(id);
			}
			_READY = true;
		}
	}
}
