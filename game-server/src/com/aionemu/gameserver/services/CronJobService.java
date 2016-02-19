package com.aionemu.gameserver.services;

import com.aionemu.commons.services.CronService;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionInstance;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * This class is used for miscellaneous long-time schedules like specific spawns.
 * 
 * @author Estrayl
 */
public class CronJobService {

	private static final CronJobService INSTANCE = new CronJobService();
	private Npc asmodianUndergroundEntrance;
	private Npc elyosUndergroundEntrance;
	private Npc moltenus;

	private CronJobService() {
		scheduleMoltenus();
		scheduleAhserionsFlight();
		scheduleIdianDepthPortalSpawns();
		//scheduleLegionDominionCalculation(); deactivated till instance is done
	}

	public static CronJobService getInstance() {
		return INSTANCE;
	}

	private void scheduleMoltenus() {
		CronService.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (moltenus != null && moltenus.isSpawned())
					return;

				int randomPos = Rnd.get(1, 3);
				SpawnTemplate template;
				switch (randomPos) {
					case 1:
						template = SpawnEngine.addNewSingleTimeSpawn(400010000, 251045, 2464.9199f, 1689f, 2882.221f, (byte) 0);
						break;
					case 2:
						template = SpawnEngine.addNewSingleTimeSpawn(400010000, 251045, 2263.4812f, 2587.1633f, 2879.5447f, (byte) 0);
						break;
					default:
						template = SpawnEngine.addNewSingleTimeSpawn(400010000, 251045, 1692.96f, 1809.04f, 2886.027f, (byte) 0);
						break;
				}
				moltenus = (Npc) SpawnEngine.spawnObject(template, 1);
				// Despawn task
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						if (moltenus != null && !moltenus.getLifeStats().isAlreadyDead()) {
							moltenus.getController().onDelete();
							moltenus = null;
						}
					}
				}, 3600 * 1000);
			}
		}, SiegeConfig.MOLTENUS_SPAWN_SCHEDULE);
	}

	private void scheduleAhserionsFlight() {
		CronService.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!AhserionInstance.getInstance().isStarted())
					AhserionInstance.getInstance().start();
			}
		}, "0 50 17 ? * SUN");
	}
	
	private void scheduleIdianDepthPortalSpawns() {
		CronService.getInstance().schedule(new Runnable() {
			
			@Override
			public void run() {
				if (asmodianUndergroundEntrance != null || elyosUndergroundEntrance != null)
					return;
				
				SpawnTemplate elyosSpawn;
				switch (Rnd.get(1, 4)) {
					case 1: // Levinshor
						elyosSpawn = SpawnEngine.addNewSingleTimeSpawn(600100000, 731631, 721.39f, 268.67f, 291.636f, (byte) 60);
						break;
					case 2: // Levinshor
						elyosSpawn = SpawnEngine.addNewSingleTimeSpawn(600100000, 731631, 332.40f, 1903.37f, 232.000f, (byte) 110);
						break;
					case 3: // Kaldor
						elyosSpawn = SpawnEngine.addNewSingleTimeSpawn(600090000, 731631, 1179.58f, 687.52f, 190.625f, (byte) 0);
						break;
					default: // Cygnea
						elyosSpawn = SpawnEngine.addNewSingleTimeSpawn(210070000, 731631, 777.01f, 1479.86f, 457.375f, (byte) 30);
						break;
				}
				SpawnTemplate asmodianSpawn;
				switch (Rnd.get(1, 4)) {
					case 1: // Levinshor
						asmodianSpawn = SpawnEngine.addNewSingleTimeSpawn(600100000, 731632, 1478.78f, 1844.20f, 225.987f, (byte) 45);
						break;
					case 2: // Levinshor
						asmodianSpawn = SpawnEngine.addNewSingleTimeSpawn(600100000, 731632, 1870.49f, 41.64f, 244.711f, (byte) 15);
						break;
					case 3: // Kaldor
						asmodianSpawn = SpawnEngine.addNewSingleTimeSpawn(600090000, 731632, 415.01f, 564.42f, 142.0f, (byte) 100);
						break;
					default: // Enshar
						asmodianSpawn = SpawnEngine.addNewSingleTimeSpawn(600090000, 731632, 233.39f, 1137.03f, 225.875f, (byte) 105);
						break;
				}
				elyosUndergroundEntrance = (Npc) SpawnEngine.spawnObject(elyosSpawn, 1);
				asmodianUndergroundEntrance = (Npc) SpawnEngine.spawnObject(asmodianSpawn, 1);
				// Despawn task
				ThreadPoolManager.getInstance().schedule(new Runnable() {
					
					@Override
					public void run() {
						if (asmodianUndergroundEntrance != null) {
							asmodianUndergroundEntrance.getController().onDelete();
							asmodianUndergroundEntrance = null;
						}
						if (elyosUndergroundEntrance != null) {
							elyosUndergroundEntrance.getController().onDelete();
							elyosUndergroundEntrance = null;
						}
					}
				}, 3600 * 1000);
			}
		}, "0 0 15,21 ? * MON,WED,FRI,SUN");
	}

	private void scheduleLegionDominionCalculation() {
		CronService.getInstance().schedule(() -> LegionDominionService.getInstance().startWeeklyCalculation(), "0 0 9 ? * WED *");
	}
}
