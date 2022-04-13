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
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.riftspawns.RiftSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.vortexspawns.VortexSpawnTemplate;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.services.rift.RiftManager;
import com.aionemu.gameserver.world.World;

/**
 * This class is responsible for NPCs spawn management. Current implementation is temporal and will be replaced in the future.
 * 
 * @author Luno
 * @modified ATracer, Source, Wakizashi, xTz, nrg
 */
public class SpawnEngine {

	private static Logger log = LoggerFactory.getLogger(SpawnEngine.class);

	/**
	 * Creates VisibleObject instance and spawns it using given {@link SpawnTemplate} instance.
	 * 
	 * @param spawn
	 * @return created and spawned VisibleObject
	 */
	public static VisibleObject spawnObject(SpawnTemplate spawn, int instanceIndex) {
		VisibleObject visObj = getSpawnedObject(spawn, instanceIndex);
		if (visObj != null && visObj.getPosition().getMapRegion() != null) {
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
	 * 
	 * @param worldId
	 * @param npcId
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @return
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
	 * Spawn all NPC's from templates
	 */
	public static void spawnAll() {
		DataManager.WORLD_MAPS_DATA.forEachParalllel(worldMapTemplate -> {
			if (!worldMapTemplate.isInstance())
				spawnBasedOnTemplate(worldMapTemplate);
		});
		DataManager.SPAWNS_DATA.clearTemplates();
		printWorldSpawnStats();
	}

	/**
	 * @param worldId
	 */
	public static void spawnWorldMap(int worldId) {
		WorldMapTemplate template = DataManager.WORLD_MAPS_DATA.getTemplate(worldId);
		if (template != null && !template.isInstance()) {
			spawnBasedOnTemplate(template);
		}
	}

	/**
	 * @param worldMapTemplate
	 */
	private static void spawnBasedOnTemplate(WorldMapTemplate worldMapTemplate) {
		int twinSpawns = worldMapTemplate.getTwinCount();
		if (twinSpawns == 0)
			twinSpawns = 1;
		twinSpawns += worldMapTemplate.getBeginnerTwinCount();
		final int mapId = worldMapTemplate.getMapId();

		for (int instanceId = 1; instanceId <= twinSpawns; instanceId++) {
			spawnInstance(mapId, instanceId, (byte) 0);
		}
	}

	public static void spawnInstance(int worldId, int instanceId, byte difficultId) {
		spawnInstance(worldId, instanceId, difficultId, 0);
	}

	/**
	 * @param worldId
	 * @param instanceId
	 */
	public static void spawnInstance(int worldId, int instanceId, byte difficultId, int ownerId) {
		List<SpawnGroup> worldSpawns = DataManager.SPAWNS_DATA.getSpawnsByWorldId(worldId);
		StaticDoorSpawnManager.spawnTemplate(worldId, instanceId);

		int spawnedCounter = 0;
		if (worldSpawns != null) {
			for (SpawnGroup spawn : worldSpawns) {
				int difficult = spawn.getDifficultId();
				if (difficult != 0 && difficult != difficultId) {
					continue;
				}

				if (spawn.isTemporarySpawn()) {
					TemporarySpawnEngine.addSpawnGroup(spawn, instanceId);
					continue;
				}

				if (spawn.getHandlerType() != null) {
					switch (spawn.getHandlerType()) {
						case RIFT:
							RiftManager.addRiftSpawnTemplate(spawn);
							break;
						case STATIC:
							StaticObjectSpawnManager.spawnTemplate(spawn, instanceId);
							break;
					}
				} else if (spawn.hasPool() && checkPool(spawn)) {
					spawn.resetTemplates(instanceId);
					for (int i = 0; i < spawn.getPool(); i++) {
						SpawnTemplate template = spawn.getRndTemplate(instanceId);
						if (template == null)
							break;
						spawnObject(template, instanceId);
						spawnedCounter++;
					}
				} else {
					for (SpawnTemplate template : spawn.getSpawnTemplates()) {
						spawnObject(template, instanceId);
						spawnedCounter++;
					}
				}
			}
			WalkerFormator.organizeAndSpawn(worldId, instanceId);
		}
		log.info("Spawned " + worldId + " [" + instanceId + "]: " + spawnedCounter);
		HousingService.getInstance().spawnHouses(worldId, instanceId, ownerId);
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
