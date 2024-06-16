package com.aionemu.gameserver.services;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.PeriodicSaveConfig;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.dao.ItemStoneListDAO;
import com.aionemu.gameserver.dao.ServerVariablesDAO;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public class PeriodicSaveService {

	private static final Logger log = LoggerFactory.getLogger(PeriodicSaveService.class);

	private final List<PeriodicSaveTask> tasks;

	public static PeriodicSaveService getInstance() {
		return SingletonHolder.instance;
	}

	private PeriodicSaveService() {
		tasks = Arrays.asList(new LegionWarehouseSaveTask(), new ServerRunTimeSaveTask());
	}

	/**
	 * Save data on shutdown
	 */
	public void onShutdown() {
		log.info("Starting data save on shutdown.");
		tasks.forEach(PeriodicSaveTask::storeDataAndCancel);
		log.info("Data successfully saved.");
	}

	private class LegionWarehouseSaveTask extends PeriodicSaveTask {

		private LegionWarehouseSaveTask() {
			super(PeriodicSaveConfig.LEGION_ITEMS * 1000);
		}

		@Override
		public void run() {
			log.info("Legion WH update task started.");
			long startTime = System.currentTimeMillis();
			int legionWhUpdated = 0;
			for (Legion legion : LegionService.getInstance().getCachedLegions()) {
				List<Item> allItems = legion.getLegionWarehouse().getItemsWithKinah();
				allItems.addAll(legion.getLegionWarehouse().getDeletedItems());
				try {
					// 1. save items first
					InventoryDAO.store(allItems, null, null, legion.getLegionId());
					// 2. save item stones
					ItemStoneListDAO.save(allItems);
				} catch (Exception ex) {
					log.error("Exception during periodic saving of legion WH", ex);
				}

				legionWhUpdated++;
			}
			long workTime = System.currentTimeMillis() - startTime;
			log.info("Legion WH update: " + workTime + " ms, legions: " + legionWhUpdated + ".");
		}
	}

	private class ServerRunTimeSaveTask extends PeriodicSaveTask {

		private ServerRunTimeSaveTask() {
			super(TimeUnit.MINUTES.toMillis(2));
		}

		@Override
		public void run() {
			ServerVariablesDAO.store("serverLastRun", System.currentTimeMillis());
		}
	}

	private abstract class PeriodicSaveTask implements Runnable {

		private final Future<?> future;

		private PeriodicSaveTask(long periodMillis) {
			future = ThreadPoolManager.getInstance().scheduleAtFixedRate(this, periodMillis, periodMillis);
		}

		private void storeDataAndCancel() {
			future.cancel(false);
			run();
		}
	}

	private static class SingletonHolder {

		protected static final PeriodicSaveService instance = new PeriodicSaveService();
	}
}
