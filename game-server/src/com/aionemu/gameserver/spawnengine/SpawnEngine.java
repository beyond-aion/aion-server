package com.aionemu.gameserver.spawnengine;

import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.templates.event.EventTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.riftspawns.RiftSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.vortexspawns.VortexSpawnTemplate;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.services.rift.RiftManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMap;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * This class is responsible for NPCs spawn management. Current implementation is temporal and will be replaced in the future.
 * 
 * @author Luno, ATracer, Source, Wakizashi, xTz, nrg
 */
public class SpawnEngine {

	private static final Logger log = LoggerFactory.getLogger(SpawnEngine.class);

	public static VisibleObject spawnObject(SpawnTemplate spawn, int instanceIndex) {
		VisibleObject visObj = getSpawnedObject(spawn, instanceIndex);
		if (visObj != null) {
			if (visObj.getSpawn() != null && visObj.getSpawn().isTemporarySpawn())
				TemporarySpawnEngine.registerSpawned(visObj);
			if (visObj.isSpawned()) // WalkerFormator.processClusteredNpc delays spawn of pooled walkers
				visObj.getPosition().getWorldMapInstance().getInstanceHandler().onSpawn(visObj);
		}
		return visObj;
	}

	private static VisibleObject getSpawnedObject(SpawnTemplate spawn, int instanceIndex) {
		int npcId = spawn.getNpcId();

		if (npcId > 400000 && npcId < 499999) {
			return VisibleObjectSpawner.spawnGatherable(spawn, instanceIndex);
		} else if (spawn instanceof BaseSpawnTemplate) {
			return VisibleObjectSpawner.spawnBaseNpc((BaseSpawnTemplate) spawn, instanceIndex);
		} else if (spawn instanceof RiftSpawnTemplate) {
			return VisibleObjectSpawner.spawnRiftNpc((RiftSpawnTemplate) spawn, instanceIndex);
		} else if (spawn instanceof SiegeSpawnTemplate) {
			return VisibleObjectSpawner.spawnSiegeNpc((SiegeSpawnTemplate) spawn, instanceIndex);
		} else if (spawn instanceof VortexSpawnTemplate) {
			return VisibleObjectSpawner.spawnInvasionNpc((VortexSpawnTemplate) spawn, instanceIndex);
		} else {
			return VisibleObjectSpawner.spawnNpc(spawn, instanceIndex);
		}
	}

	/**
	 * Create non-permanent spawn template with no respawn
	 */
	public static SpawnTemplate newSingleTimeSpawn(int worldId, int npcId, float x, float y, float z, byte heading) {
		return newSpawn(worldId, npcId, x, y, z, heading, 0, 0, null);
	}

	public static SpawnTemplate newSingleTimeSpawn(int worldId, int npcId, float x, float y, float z, byte heading, int creatorId) {
		return newSpawn(worldId, npcId, x, y, z, heading, 0, creatorId, null);
	}

	public static SpawnTemplate newSingleTimeSpawn(int worldId, int npcId, float x, float y, float z, byte heading, int creatorId,
		String aiName) {
		return newSpawn(worldId, npcId, x, y, z, heading, 0, creatorId, aiName);
	}

	public static SpawnTemplate newSpawn(int worldId, int npcId, float x, float y, float z, byte heading, int respawnTime) {
		return newSpawn(worldId, npcId, x, y, z, heading, respawnTime, 0, null);
	}

	private static SpawnTemplate newSpawn(int worldId, int npcId, float x, float y, float z, byte heading, int respawnTime, int creatorId,
		String aiName) {
		return new SpawnTemplate(new SpawnGroup(worldId, npcId, respawnTime), x, y, z, heading, 0, null, 0, 0, creatorId, aiName);
	}

	/**
	 * Should be used when you need to add a siegespawn through code and not from static_data spawns (e.g. CustomBalaurAssault)
	 */
	public static SiegeSpawnTemplate newSiegeSpawn(int worldId, int npcId, int siegeId, SiegeRace race, SiegeModType mod, float x, float y, float z,
		byte heading) {
		return new SiegeSpawnTemplate(siegeId, race, mod, new SpawnGroup(worldId, npcId, 0), x, y, z, heading, 0, null, 0, 0);
	}

	static void bringIntoWorld(VisibleObject visibleObject, SpawnTemplate spawn, int instanceIndex) {
		bringIntoWorld(visibleObject, spawn.getWorldId(), instanceIndex, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getHeading());
	}

	public static void bringIntoWorld(VisibleObject visibleObject, int worldId, int instanceIndex, float x, float y, float z, byte h) {
		World world = World.getInstance();
		world.storeObject(visibleObject);
		world.setPosition(visibleObject, worldId, instanceIndex, x, y, z, h);
		world.spawn(visibleObject);
	}

	public static void bringIntoWorld(VisibleObject visibleObject) {
		if (visibleObject.getPosition() == null)
			throw new IllegalArgumentException("Position is null");
		World world = World.getInstance();
		world.storeObject(visibleObject);
		world.spawn(visibleObject);
	}

	/**
	 * Spawn all NPCs from templates
	 */
	public static void spawnAll() {
		DataManager.WORLD_MAPS_DATA.forEachParalllel(worldMapTemplate -> {
			WorldMap worldMap = World.getInstance().getWorldMap(worldMapTemplate.getMapId());
			if (!worldMap.isInstanceType())
				worldMap.forEach(instance -> spawnInstance(instance, (byte) 0, instance.getOwnerId()));
		});
		DataManager.SPAWNS_DATA.clearTemplates();
		printWorldSpawnStats();
	}

	public static void spawnInstance(WorldMapInstance instance, byte difficultId, int ownerId) {
		spawnInstance(instance, difficultId, ownerId, null);
	}

	public static void spawnEventSpawns(WorldMapInstance instance, byte difficultId, int ownerId, EventTemplate eventTemplate) {
		spawnInstance(instance, difficultId, ownerId, eventTemplate);
	}

	private static void spawnInstance(WorldMapInstance instance, byte difficultId, int ownerId, EventTemplate eventTemplate) {
		List<SpawnGroup> worldSpawns = DataManager.SPAWNS_DATA.getSpawnsByWorldId(instance.getMapId());
		if (eventTemplate == null)
			StaticDoorSpawnManager.spawnTemplate(instance);

		int spawnedCounter = 0;
		if (worldSpawns != null) {
			for (SpawnGroup spawn : worldSpawns) {
				if (eventTemplate != null && eventTemplate != spawn.getEventTemplate())
					continue;
				if (spawn.getDifficultId() != 0 && spawn.getDifficultId() != difficultId)
					continue;

				if (spawn.isTemporarySpawn()) {
					TemporarySpawnEngine.addSpawnGroup(spawn, instance.getInstanceId());
					if (!spawn.getTemporarySpawn().isInSpawnTime())
						continue;
				}

				if (spawn.getHandlerType() != null) {
					switch (spawn.getHandlerType()) {
						case RIFT -> RiftManager.addRiftSpawnTemplate(spawn);
						case STATIC -> StaticObjectSpawnManager.spawnTemplate(spawn, instance.getInstanceId());
					}
				} else if (spawn.hasPool() && checkPool(spawn)) {
					spawn.resetTemplates(instance.getInstanceId());
					for (int i = 0; i < spawn.getPool(); i++) {
						SpawnTemplate template = spawn.getRndTemplate(instance.getInstanceId());
						if (template == null)
							break;
						spawnObject(template, instance.getInstanceId());
						spawnedCounter++;
					}
				} else {
					for (SpawnTemplate template : spawn.getSpawnTemplates()) {
						if (template.getTemporarySpawn() != null && !template.getTemporarySpawn().isInSpawnTime())
							continue;
						spawnObject(template, instance.getInstanceId());
						spawnedCounter++;
					}
				}
			}
			if (eventTemplate == null)
				WalkerFormator.organizeAndSpawn(instance.getMapId(), instance.getInstanceId());
		}
		if (spawnedCounter > 0) {
			if (eventTemplate == null)
				log.info("Spawned " + spawnedCounter + " objects in " + instance);
			else
				log.info('[' + eventTemplate.getName() + "] Spawned " + spawnedCounter + " event objects in " + instance);
		}
		if (eventTemplate == null)
			HousingService.getInstance().spawnHouses(instance, ownerId);
	}

	public static boolean checkPool(SpawnGroup spawn) {
		if (spawn.getPool() >= spawn.getSpawnTemplates().size()) {
			log.warn("Spawn pool size must be smaller than spots to take effect, npcId: " + spawn.getNpcId() + ", worldId: " + spawn.getWorldId());
			return false;
		}
		return true;
	}

	public static void printWorldSpawnStats() {
		StatsCollector function = new StatsCollector();
		World.getInstance().forEachObject(function);
		log.info("Loaded " + function.getNpcCount() + " npc spawns");
		log.info("Loaded " + function.getGatherableCount() + " gatherable spawns");
	}

	static class StatsCollector implements Consumer<VisibleObject> {

		int npcCount;
		int gatherableCount;

		@Override
		public void accept(VisibleObject object) {
			if (object instanceof Npc) {
				npcCount++;
			} else if (object instanceof Gatherable) {
				gatherableCount++;
			}
		}

		public int getNpcCount() {
			return npcCount;
		}

		public int getGatherableCount() {
			return gatherableCount;
		}

	}

}
