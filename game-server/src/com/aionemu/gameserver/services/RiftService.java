package com.aionemu.gameserver.services;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.aionemu.commons.services.CronService;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.schedule.RiftSchedule;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.rift.RiftLocation;
import com.aionemu.gameserver.model.templates.rift.OpenRift;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.riftspawns.RiftSpawnTemplate;
import com.aionemu.gameserver.services.rift.RiftInformer;
import com.aionemu.gameserver.services.rift.RiftManager;
import com.aionemu.gameserver.services.rift.RiftOpenRunnable;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.world.World;

/**
 * @author Source
 */
public class RiftService {

	private RiftSchedule schedule;
	private Map<Integer, RiftLocation> locations;
	private Map<Integer, RiftLocation> activeRifts = new LinkedHashMap<>();
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
			return RiftService.getInstance().getRiftLocations().containsKey(id);
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
			if (!guards && Rnd.nextBoolean())
				return;
		}
		List<RiftLocation> possibleLocs = new ArrayList<>();
		for (RiftLocation loc : locations.values()) {
			if (loc.getWorldId() == id) {
				if (guards) {
					if (loc.hasSpawns() && loc.isAutoCloseable())
						possibleLocs.add(loc);
					continue;
				}
				if (!loc.hasSpawns() && loc.isAutoCloseable())
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

	public void openRifts(RiftLocation location, boolean isWithGuards) {
		location.setOpened(true);

		// Spawn NPC guards
		if (isWithGuards) {
			List<SpawnGroup> locSpawns = DataManager.SPAWNS_DATA.getRiftSpawnsByLocId(location.getId());
			for (SpawnGroup group : locSpawns) {
				for (SpawnTemplate st : group.getSpawnTemplates()) {
					RiftSpawnTemplate template = (RiftSpawnTemplate) st;
					location.addSpawned(SpawnEngine.spawnObject(template, 1));
				}
			}
		}

		// Spawn rifts
		RiftManager.getInstance().spawnRift(location, isWithGuards);
		activeRifts.put(location.getId(), location);
	}

	public void closeRift(RiftLocation location) {
		location.setOpened(false);

		// Despawn rift NPCs and cancel their respawns
		for (Map.Entry<Integer, SpawnTemplate> entry : location.getSpawned().entrySet()) {
			int npcObjectId = entry.getKey();
			SpawnTemplate npcSpawnTemplate = entry.getValue();
			VisibleObject npc = World.getInstance().findVisibleObject(npcObjectId);
			// npcObjectId may have been released and reassigned to a completely unrelated mob, if the original (now despawned) rift npc was a non
			// respawning mob. spawned list doesn't clean up removed spawns but only updates respawning npcObjectIds, therefore the npcSpawnTemplate check
			if (npc != null && npc.getSpawn() == npcSpawnTemplate)
				npc.getController().deleteIfAliveOrCancelRespawn();
			else
				RespawnService.cancelRespawn(npcObjectId, npcSpawnTemplate);
		}

		// Clear spawned list
		location.getSpawned().clear();
	}

	public boolean isRiftOpened(int riftId) {
		return activeRifts.containsKey(riftId);
	}

	public void closeRifts(boolean forceClose) {
		closing.lock();
		try {
			List<Integer> riftsToRemove = new ArrayList<>();
			for (RiftLocation rift : activeRifts.values()) {
				if (forceClose || rift.isAutoCloseable()) {
					closeRift(rift);
					riftsToRemove.add(rift.getId());
				}
			}
			riftsToRemove.forEach(riftId -> activeRifts.remove(riftId));
		} finally {
			closing.unlock();
		}
	}

	public void updateSpawned(int oldObjectId, VisibleObject respawn) {
		for (RiftLocation loc : locations.values()) {
			if (loc.replaceSpawned(oldObjectId, respawn))
				break;
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
