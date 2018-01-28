package com.aionemu.gameserver.spawnengine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.TemporarySpawn;
import com.aionemu.gameserver.services.RespawnService;

/**
 * @author xTz, Neon
 */
public class TemporarySpawnEngine {

	private static final Map<SpawnGroup, Set<Integer>> spawnGroups = new ConcurrentHashMap<>();
	private static List<VisibleObject> spawnedObjects = new ArrayList<>();

	public static void spawnAll() {
		spawn(true);
	}

	public static void onHourChange() {
		despawn();
		synchronized (spawnGroups) { // sync to avoid incorrect spawns on concurrent onInstanceDestroy call
			spawn(false);
		}
	}

	private static void despawn() {
		// no sync on spawnedObjects needed since despawn() and spawn() always occur in the same thread
		List<VisibleObject> remainingObjects = new ArrayList<>(spawnedObjects.size());
		spawnedObjects.forEach(object -> {
			if (object.getSpawn().getTemporarySpawn().canDespawn()) {
				if (object instanceof Npc) {
					Npc npc = (Npc) object;
					if (!npc.isDead() && object.getSpawn().hasPool())
						object.getSpawn().setUse(npc.getInstanceId(), false);
				}
				if (!object.getController().delete())
					RespawnService.cancelRespawn(object);
			} else {
				remainingObjects.add(object);
			}
		});
		spawnedObjects = remainingObjects;
	}

	private static void spawn(boolean startCheck) {
		for (Entry<SpawnGroup, Set<Integer>> entry : spawnGroups.entrySet()) {
			Set<Integer> instanceIds = entry.getValue();
			if (instanceIds.isEmpty())
				continue;
			SpawnGroup spawn = entry.getKey();
			if (spawn.hasPool()) {
				TemporarySpawn temporarySpawn = spawn.getTemporarySpawn();
				if (temporarySpawn.canSpawn() || (startCheck && temporarySpawn.isInSpawnTime())) {
					for (Integer instanceId : instanceIds) {
						spawn.resetTemplates(instanceId);
						for (int pool = 0; pool < spawn.getPool(); pool++) {
							SpawnTemplate template = spawn.getRndTemplate(instanceId);
							spawn(template, instanceId);
						}
					}
				}
			} else {
				for (SpawnTemplate template : spawn.getSpawnTemplates()) {
					TemporarySpawn temporarySpawn = template.getTemporarySpawn();
					if (temporarySpawn.canSpawn() || (startCheck && temporarySpawn.isInSpawnTime()))
						spawn(template, instanceIds);
				}
			}
		}
	}

	private static void spawn(SpawnTemplate template, Set<Integer> instanceIds) {
		instanceIds.forEach(instanceId -> spawn(template, instanceId));
	}

	private static void spawn(SpawnTemplate template, int instanceId) {
		VisibleObject object = SpawnEngine.spawnObject(template, instanceId);
		if (object != null)
			spawnedObjects.add(object);
	}

	public static void addSpawnGroup(SpawnGroup spawnGroup, int instanceId) {
		spawnGroups.compute(spawnGroup, (spawn, instances) -> {
			if (instances == null) {
				instances = ConcurrentHashMap.newKeySet();
			}
			instances.add(instanceId);
			return instances;
		});
	}

	public static void onInstanceDestroy(int worldId, int instanceId) {
		synchronized (spawnGroups) {
			spawnGroups.forEach((spawnGroup, instanceIds) -> {
				if (spawnGroup.getWorldId() == worldId)
					instanceIds.remove(instanceId);
				if (instanceIds.isEmpty())
					spawnGroups.remove(spawnGroup);
			});
		}
	}
}
