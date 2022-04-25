package com.aionemu.gameserver.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.utils.ConsoleUtil;
import com.aionemu.gameserver.configs.main.CleaningConfig;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dataholders.PlayerExperienceTable;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.xml.JAXBUtil;

/**
 * Offers the functionality to delete all data about inactive players
 *
 * @author nrg
 * @reworked Neon
 */
public class DatabaseCleaningService {

	private static Logger log = LoggerFactory.getLogger(DatabaseCleaningService.class);
	private static DatabaseCleaningService instance = new DatabaseCleaningService();
	private List<Worker> workers = new ArrayList<>();

	/**
	 * Private constructor to avoid external access and ensure singleton
	 */
	private DatabaseCleaningService() {
	}

	/**
	 * Cleans the database from inactive player data
	 */
	public void runCleaning() {
		if (workers == null || workers.size() > 0) // just in case
			throw new UnsupportedOperationException("DatabaseCleaningService can only be executed once and only on server startup!");

		// only a security feature
		if (CleaningConfig.MIN_ACCOUNT_INACTIVITY_DAYS <= 30) {
			log.warn("The configured days for database cleaning is too low. For security reasons the service will only execute with periods over 30 days!");
			return;
		}

		try {
			log.info("DatabaseCleaningService: Executing database cleaning");

			long startTime = System.currentTimeMillis();
			File xml = new File("./data/static_data/player_experience_table.xml"); // fast unmarshal to avoid loading whole static data before
			PlayerExperienceTable pxt = JAXBUtil.deserialize(xml, PlayerExperienceTable.class, "./data/static_data/static_data.xsd");
			long maxExp = pxt.getStartExpForLevel(CleaningConfig.MAX_DELETABLE_CHAR_LEVEL + 1) - 1;
			Set<Integer> accountIds = DAOManager.getDAO(PlayerDAO.class).getInactiveAccounts(CleaningConfig.MIN_ACCOUNT_INACTIVITY_DAYS);
			CountDownLatch cl = delegateToThreads(accountIds, maxExp);
			if (cl != null) {
				try {
					cl.await();
				} catch (InterruptedException e) {
					logStatistics("Database cleaning aborted!", startTime);
					return;
				}
			}
			logStatistics("Database cleaning finished!", startTime);
		} finally {
			workers = null;
		}
	}

	private void logStatistics(String title, long startTime) {
		int chars = 0, accs = 0;
		for (Worker w : workers) {
			chars += w.deletedChars;
			accs += w.affectedAccounts;
		}
		log.info(title + " Deleted " + chars + " chars from " + accs + " accounts in " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
	}

	private CountDownLatch delegateToThreads(Set<Integer> idsToDelegate, long maxExp) {
		// every id to another worker with maximum of n different workers
		Iterator<Integer> i = idsToDelegate.iterator();
		int toDelete = 0;
		for (int workerNo = 0; i.hasNext() && toDelete < CleaningConfig.TOTAL_ACC_LIMIT; workerNo = ++workerNo % CleaningConfig.WORKER_THREADS) {
			if (workerNo >= workers.size()) {
				workers.add(new Worker());
			}
			workers.get(workerNo).ids.add(i.next());
			toDelete++;
		}
		if (toDelete > 0) {
			log.info("DatabaseCleaningService: Executing deletion over " + workers.size() + " longrunning threads");
			ConsoleUtil.initAndPrintProgressBar(toDelete);
			CountDownLatch cl = new CountDownLatch(workers.size());
			for (Worker w : workers) // get them working on our long-running
				ThreadPoolManager.getInstance().executeLongRunning(() -> w.work(cl, maxExp));
			return cl;
		}
		return null;
	}

	/**
	 * The only external access
	 *
	 * @return a singleton DatabaseCleaningService
	 */
	public static DatabaseCleaningService getInstance() {
		return instance;
	}

	private class Worker {

		private List<Integer> ids = new ArrayList<>();
		private int deletedChars = 0;
		private int affectedAccounts = 0;

		public void work(CountDownLatch cl, long maxExp) {
			for (int id : ids) {
				int chars = PlayerService.deleteAccountsCharsFromDB(id, maxExp);
				if (chars > 0) {
					deletedChars += chars;
					affectedAccounts++;
				}
				ConsoleUtil.increaseAndPrintProgress();
			}
			cl.countDown();
		}
	}
}
