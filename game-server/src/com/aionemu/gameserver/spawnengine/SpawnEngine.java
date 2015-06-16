package com.aionemu.gameserver.spawnengine;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.administration.DeveloperConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup2;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.riftspawns.RiftSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.vortexspawns.VortexSpawnTemplate;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.services.rift.RiftManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * This class is responsible for NPCs spawn management. Current implementation is temporal and will be replaced in the
 * future.
 * 
 * @author Luno modified by ATracer, Source, Wakizashi, xTz, nrg
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
		final VisibleObject visObj = getSpawnedObject(spawn, instanceIndex);
		if (spawn.isEventSpawn())
			spawn.getEventTemplate().addSpawnedObject(visObj);

		spawn.setVisibleObject(visObj);
		return visObj;
	}

	private static VisibleObject getSpawnedObject(SpawnTemplate spawn, int instanceIndex) {
		int objectId = spawn.getNpcId();

		if (objectId > 400000 && objectId < 499999) {
			return VisibleObjectSpawner.spawnGatherable(spawn, instanceIndex);
		}
		else if (spawn instanceof BaseSpawnTemplate) {
			return VisibleObjectSpawner.spawnBaseNpc((BaseSpawnTemplate) spawn, instanceIndex);
		}
		else if (spawn instanceof RiftSpawnTemplate) {
			return VisibleObjectSpawner.spawnRiftNpc((RiftSpawnTemplate) spawn, instanceIndex);
		}
		else if (spawn instanceof SiegeSpawnTemplate) {
			return VisibleObjectSpawner.spawnSiegeNpc((SiegeSpawnTemplate) spawn, instanceIndex);
		}
		else if (spawn instanceof VortexSpawnTemplate) {
			return VisibleObjectSpawner.spawnInvasionNpc((VortexSpawnTemplate) spawn, instanceIndex);
		}
		else {
			return VisibleObjectSpawner.spawnNpc(spawn, instanceIndex);
		}
	}

	/**
	 * @param worldId
	 * @param npcId
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @return
	 */
	static SpawnTemplate createSpawnTemplate(int worldId, int npcId, float x, float y, float z, byte heading) {
		return new SpawnTemplate(new SpawnGroup2(worldId, npcId), x, y, z, heading, 0, null, 0, 0);
	}

	static SpawnTemplate createSpawnTemplate(int worldId, int npcId, float x, float y, float z, byte heading, int creatorId, String masterName) {
		SpawnTemplate template = createSpawnTemplate(worldId, npcId, x, y, z, heading);
		template.setCreatorId(creatorId);
		template.setMasterName(masterName);
		return template;
	}

	/**
	 * Should be used when you need to add a siegespawn through code and not from static_data spawns (e.g.
	 * CustomBalaurAssault)
	 */
	public static SiegeSpawnTemplate addNewSiegeSpawn(int worldId, int npcId, int siegeId, SiegeRace race, SiegeModType mod, float x,
		float y, float z, byte heading) {
		SiegeSpawnTemplate spawnTemplate = new SiegeSpawnTemplate(new SpawnGroup2(worldId, npcId), x, y, z, heading, 0, null, 0, 0);
		spawnTemplate.setSiegeId(siegeId);
		spawnTemplate.setSiegeRace(race);
		spawnTemplate.setSiegeModType(mod);
		return spawnTemplate;
	}

	/**
	 * Should be used when need to define whether spawn will be deleted after death Using this method spawns will not be
	 * saved with //save_spawn command
	 * 
	 * @param worldId
	 * @param npcId
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param respawnTime
	 * @param permanent
	 * @return SpawnTemplate
	 */
	public static SpawnTemplate addNewSpawn(int worldId, int npcId, float x, float y, float z, byte heading, int respawnTime) {
		SpawnTemplate spawnTemplate = createSpawnTemplate(worldId, npcId, x, y, z, heading);
		spawnTemplate.setRespawnTime(respawnTime);
		return spawnTemplate;
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
	public static SpawnTemplate addNewSingleTimeSpawn(int worldId, int npcId, float x, float y, float z, byte heading) {
		return addNewSpawn(worldId, npcId, x, y, z, heading, 0);
	}

	public static SpawnTemplate addNewSingleTimeSpawn(int worldId, int npcId, float x, float y, float z, byte heading, int creatorId,
		String masterName) {
		SpawnTemplate template = addNewSpawn(worldId, npcId, x, y, z, heading, 0);
		template.setCreatorId(creatorId);
		template.setMasterName(masterName);
		return template;
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
		if (!DeveloperConfig.SPAWN_ENABLE) {
			log.info("Spawns are disabled");
			return;
		}
		for (WorldMapTemplate worldMapTemplate : DataManager.WORLD_MAPS_DATA) {
			if (worldMapTemplate.isInstance()) {
				continue;
			}
			spawnBasedOnTemplate(worldMapTemplate);
		}
		DataManager.SPAWNS_DATA2.clearTemplates();
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
		twinSpawns +=  worldMapTemplate.getBeginnerTwinCount();
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
		List<SpawnGroup2> worldSpawns = DataManager.SPAWNS_DATA2.getSpawnsByWorldId(worldId);
		WorldMapTemplate worldTemplate = DataManager.WORLD_MAPS_DATA.getTemplate(worldId);
		StaticDoorSpawnManager.spawnTemplate(worldId, instanceId);

		int spawnedCounter = 0;
		if (worldSpawns != null) {
			for (SpawnGroup2 spawn : worldSpawns) {
				int difficult = spawn.getDifficultId();
				if (difficult != 0 && difficult != difficultId) {
					continue;
				}

				// Disable temporary spawns in instances, TemporarySpawnEngine
				// doesn't support removing spawns
				if (spawn.isTemporarySpawn() && !worldTemplate.isInstance()) {
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
						default:
							break;
					}
				}
				else if (spawn.hasPool() && checkPool(spawn)) {
					spawn.resetTemplates(instanceId);
					for (int i = 0; i < spawn.getPool(); i++) {
						SpawnTemplate template = spawn.getRndTemplate(instanceId);
						if (template == null)
							break;
						spawnObject(template, instanceId);
						spawnedCounter++;
					}
				}
				else {
					for (SpawnTemplate template : spawn.getSpawnTemplates()) {
						spawnObject(template, instanceId);
						spawnedCounter++;
					}
				}
			}
			WalkerFormator.organizeAndSpawn(worldId, instanceId);
		}
		log.info("Spawned " + worldId + " [" + instanceId + "] : " + spawnedCounter);
		HousingService.getInstance().spawnHouses(worldId, instanceId, ownerId);
	}

	private static boolean checkPool(SpawnGroup2 spawn) {
		if (spawn.getSpawnTemplates().size() < spawn.getPool()) {
			log.warn("Pool size more then spots, npcId: " + spawn.getNpcId() + ", worldId: " + spawn.getWorldId());
			return false;
		}
		return true;
	}

	public static void printWorldSpawnStats() {
		StatsCollector visitor = new StatsCollector();
		World.getInstance().doOnAllObjects(visitor);
		log.info("Loaded " + visitor.getNpcCount() + " npc spawns");
		log.info("Loaded " + visitor.getGatherableCount() + " gatherable spawns");
		QuestEngine.getInstance().printMissingSpawns();
	}

	static class StatsCollector implements Visitor<VisibleObject> {

		int npcCount;
		int gatherableCount;

		@Override
		public void visit(VisibleObject object) {
			if (object instanceof Npc) {
				npcCount++;
			}
			else if (object instanceof Gatherable) {
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
