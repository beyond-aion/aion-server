package com.aionemu.gameserver.services;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.aionemu.commons.services.CronService;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.shedule.RiftSchedule;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.rift.RiftLocation;
import com.aionemu.gameserver.model.templates.rift.OpenRift;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup2;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.riftspawns.RiftSpawnTemplate;
import com.aionemu.gameserver.services.rift.RiftInformer;
import com.aionemu.gameserver.services.rift.RiftManager;
import com.aionemu.gameserver.services.rift.RiftOpenRunnable;
import com.aionemu.gameserver.spawnengine.SpawnEngine;

import javolution.util.FastMap;
import javolution.util.FastTable;

/**
 * @author Source
 */
public class RiftService {

	private RiftSchedule schedule;
	private Map<Integer, RiftLocation> locations;
	private Map<Integer, RiftLocation> activeRifts = new FastMap<>();
	private final Lock closing = new ReentrantLock();

	public void initRiftLocations() {
		if (CustomConfig.RIFT_ENABLED) {
			locations = DataManager.RIFT_DATA.getRiftLocations();
		} else {
			locations = Collections.emptyMap();
		}
	}

	public void initRifts() {
		if (CustomConfig.RIFT_ENABLED) {
			schedule = RiftSchedule.load();
			for (RiftSchedule.Rift rift : schedule.getRiftsList())
				for (OpenRift open : rift.getRift())
					CronService.getInstance().schedule(new RiftOpenRunnable(rift.getWorldId(), open.spawnGuards()), open.getSchedule());
		}
	}

	public boolean isValidId(int id) {
		if (isRift(id)) {
			return RiftService.getInstance().getRiftLocations().keySet().contains(id);
		} else {
			for (RiftLocation loc : RiftService.getInstance().getRiftLocations().values()) {
				if (loc.getWorldId() == id)
					return true;
			}
		}

		return false;
	}

	private boolean isRift(int id) {
		return id < 10000;
	}

	public boolean openRifts(int id, boolean guards) {
		if (isValidId(id)) {
			if (isRift(id)) {
				RiftLocation rift = getRiftLocation(id);
				if (rift.getSpawned().isEmpty()) {
					openRifts(rift, guards);

					// Broadcast rift spawn on map
					RiftInformer.sendRiftsInfo(rift.getWorldId());
					return true;
				}
			} else {
				boolean opened = false;
				for (RiftLocation rift : getRiftLocations().values()) {
					if (rift.getWorldId() == id && rift.getSpawned().isEmpty()) {
						openRifts(rift, guards);
						opened = true;
					}
				}

				// Broadcast rift spawn on map
				RiftInformer.sendRiftsInfo(id);
				return opened;
			}
		}
		return false;
	}

	public boolean closeRifts(int id) {
		if (isValidId(id)) {
			if (isRift(id)) {
				RiftLocation rift = getRiftLocation(id);
				if (!rift.getSpawned().isEmpty()) {
					closeRift(rift);
					return true;
				}
			} else {
				boolean opened = false;
				for (RiftLocation rift : getRiftLocations().values()) {
					if (rift.getWorldId() == id && !rift.getSpawned().isEmpty()) {
						closeRift(rift);
						opened = true;
					}
				}
				return opened;
			}
		}
		return false;
	}

	/**
	 * Just a work-around, this stuff needs refactoring
	 * 
	 * @param id
	 *          better use map IDs
	 */
	public void prepareRiftOpening(int id, boolean guards) {
		if (id != 210070000 && id != 220080000) {
			if (!guards && Rnd.get(1, 100) > 50)
				return;
		}
		List<RiftLocation> possibleLocs = new FastTable<>();
		for (RiftLocation loc : locations.values()) {
			if (loc.getWorldId() == id) {
				if (guards) {
					if (loc.hasSpawns())
						possibleLocs.add(loc);
					continue;
				}
				if (!loc.hasSpawns())
					possibleLocs.add(loc);
			}
		}
		
		int maxCount = 1;
		if (!guards) {
			switch (id) {
				case 210050000:
				case 220070000:
					maxCount = 3;
					break;
				default:
					maxCount = 4;
			}
		}
		
		int count = Rnd.get(1, maxCount);
		while (possibleLocs.size() > count)
			possibleLocs.remove(Rnd.get(0, possibleLocs.size() - 1));
		
		for (RiftLocation loc : possibleLocs)
			openRifts(loc, guards);
	}

	public void openRifts(RiftLocation location, boolean guards) {
		location.setOpened(true);

		// Spawn NPC guards
		if (guards) {
			location.setWithGuards(true);
			List<SpawnGroup2> locSpawns = DataManager.SPAWNS_DATA2.getRiftSpawnsByLocId(location.getId());
			for (SpawnGroup2 group : locSpawns) {
				for (SpawnTemplate st : group.getSpawnTemplates()) {
					RiftSpawnTemplate template = (RiftSpawnTemplate) st;
					location.getSpawned().add((Npc) SpawnEngine.spawnObject(template, 1));
				}
			}
		}

		// Spawn rifts
		RiftManager.getInstance().spawnRift(location);
		activeRifts.put(location.getId(), location);
	}

	public void closeRift(RiftLocation location) {
		location.setOpened(false);

		// Despawn NPC
		for (Npc rift : location.getSpawned()) {
			if (rift == null)
				continue;
			rift.getController().cancelTask(TaskId.RESPAWN);
			rift.getController().delete();
		}

		// Clear spawned list
		location.getSpawned().clear();
	}

	public void closeRifts() {
		closing.lock();

		try {
			for (RiftLocation rift : activeRifts.values())
				closeRift(rift);

			activeRifts.clear();
		} finally {
			closing.unlock();
		}
	}

	public int getDuration() {
		return CustomConfig.RIFT_DURATION;
	}

	public RiftLocation getRiftLocation(int id) {
		return locations.get(id);
	}

	public Map<Integer, RiftLocation> getRiftLocations() {
		return locations;
	}

	public static RiftService getInstance() {
		return RiftServiceHolder.INSTANCE;
	}

	private static class RiftServiceHolder {

		private static final RiftService INSTANCE = new RiftService();
	}

}
