package com.aionemu.gameserver.spawnengine;

import java.util.*;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.event.EventTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
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
				object.getController().deleteIfAliveOrCancelRespawn();
			} else {
				remainingObjects.add(object);
			}
		});
		spawnedObjects.retainAll(remainingObjects);
	}

	private static void spawn() {
		Map<SpawnGroup, List<VisibleObject>> spawnedBySpawnGroup = spawnedObjects.stream().collect(Collectors.groupingBy(o -> o.getSpawn().getGroup()));
		spawnGroups.forEach((spawn, instanceIds) -> {
			if (instanceIds.isEmpty())
				return;
			List<VisibleObject> spawned = spawnedBySpawnGroup.getOrDefault(spawn, Collections.emptyList());
			if (spawn.hasPool()) {
				if (!spawn.getTemporarySpawn().canSpawn())
					return;
				Set<Integer> spawnableInstanceIds = new HashSet<>(instanceIds);
				spawned.forEach(o -> spawnableInstanceIds.remove(o.getInstanceId()));
				for (Integer instanceId : spawnableInstanceIds) {
					spawn.resetPoolSpots(instanceId);
					for (int pool = 0; pool < spawn.getPool(); pool++) {
						SpawnTemplate template = spawn.reserveRandomFreePoolSpot(instanceId);
						SpawnEngine.spawnObject(template, instanceId);
					}
				}
			} else {
				for (SpawnTemplate template : spawn.getSpawnTemplates()) {
					if (!template.getTemporarySpawn().canSpawn())
						continue;
					Set<Integer> spawnableInstanceIds = new HashSet<>(instanceIds);
					spawned.stream().filter(o -> o.getSpawn().equals(template)).forEach(o -> spawnableInstanceIds.remove(o.getInstanceId()));
					spawnableInstanceIds.forEach(instanceId -> SpawnEngine.spawnObject(template, instanceId));
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
