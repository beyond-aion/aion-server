package com.aionemu.gameserver.spawnengine;

import java.util.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.event.EventTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.TemporarySpawn;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz, Neon
 */
public class TemporarySpawnEngine {

	private static final Map<SpawnGroup, Set<Integer>> spawnGroups = new HashMap<>();
	private static final Set<VisibleObject> spawnedObjects = new HashSet<>();

	public static synchronized void onHourChange() {
		despawn();
		spawn();
	}

	private static void despawn() {
		List<VisibleObject> remainingObjects = new ArrayList<>(spawnedObjects.size());
		spawnedObjects.forEach(object -> {
			if (object.getSpawn().getTemporarySpawn().canDespawn()) {
				if (object instanceof Npc npc && !npc.isDead() && object.getSpawn().hasPool())
					object.getSpawn().setUse(npc.getInstanceId(), false);
				object.getController().deleteIfAliveOrCancelRespawn();
			} else {
				remainingObjects.add(object);
			}
		});
		spawnedObjects.retainAll(remainingObjects);
	}

	private static void spawn() {
		spawnGroups.forEach((spawn, instanceIds) -> {
			if (instanceIds.isEmpty())
				return;
			if (spawn.hasPool()) {
				TemporarySpawn temporarySpawn = spawn.getTemporarySpawn();
				if (temporarySpawn.canSpawn()) {
					for (Integer instanceId : instanceIds) {
						spawn.resetTemplates(instanceId);
						for (int pool = 0; pool < spawn.getPool(); pool++) {
							SpawnTemplate template = spawn.getRndTemplate(instanceId);
							SpawnEngine.spawnObject(template, instanceId);
						}
					}
				}
			} else {
				for (SpawnTemplate template : spawn.getSpawnTemplates()) {
					TemporarySpawn temporarySpawn = template.getTemporarySpawn();
					if (temporarySpawn.canSpawn())
						instanceIds.forEach(instanceId -> SpawnEngine.spawnObject(template, instanceId));
				}
			}
		});
	}

	public static synchronized void registerSpawned(VisibleObject object) {
		spawnedObjects.add(object);
	}

	public static synchronized void unregisterSpawned(int objectId) {
		spawnedObjects.removeIf(o -> o.getObjectId() == objectId);
	}

	public static synchronized void addSpawnGroup(SpawnGroup spawnGroup, int instanceId) {
		spawnGroups.computeIfAbsent(spawnGroup, k -> new HashSet<>()).add(instanceId);
	}

	public static synchronized void unregister(EventTemplate eventTemplate) {
		spawnedObjects.removeIf(o -> o.getSpawn().getEventTemplate() == eventTemplate);
		spawnGroups.keySet().removeIf(s -> s.getEventTemplate() == eventTemplate);
	}

	public static synchronized void onInstanceDestroy(WorldMapInstance instance) {
		spawnedObjects.removeIf(o -> instance.equals(o.getWorldMapInstance()));
		spawnGroups.forEach((spawnGroup, instanceIds) -> {
			if (spawnGroup.getWorldId() == instance.getMapId())
				instanceIds.remove(instance.getInstanceId());
		});
	}
}
