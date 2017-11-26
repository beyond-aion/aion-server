package com.aionemu.gameserver.spawnengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.TemporarySpawn;
import com.aionemu.gameserver.services.RespawnService;

/**
 * @author xTz
 */
public class TemporarySpawnEngine {

	private static final List<SpawnGroup> temporarySpawns = new ArrayList<>();
	private static final Map<SpawnGroup, HashSet<Integer>> tempSpawnInstanceMap = new HashMap<>();

	public static void spawnAll() {
		spawn(true);
	}

	public static void onHourChange() {
		despawn();
		spawn(false);
	}

	private static void despawn() {
		for (SpawnGroup spawn : temporarySpawns) {
			for (SpawnTemplate template : spawn.getSpawnTemplates()) {
				if (template.getTemporarySpawn().canDespawn()) {
					VisibleObject object = template.getVisibleObject();
					if (object == null) {
						continue;
					}
					if (object instanceof Npc) {
						Npc npc = (Npc) object;
						if (!npc.isDead() && template.hasPool()) {
							spawn.setTemplateUse(npc.getInstanceId(), template, false);
						}
					}
					if (!object.getController().delete())
						RespawnService.cancelRespawn(object);
				}
			}
		}
	}

	private static void spawn(boolean startCheck) {
		for (SpawnGroup spawn : temporarySpawns) {
			HashSet<Integer> instances = tempSpawnInstanceMap.get(spawn);
			if (spawn.hasPool()) {
				TemporarySpawn temporarySpawn = spawn.getTemporarySpawn();
				if (temporarySpawn.canSpawn() || (startCheck && spawn.getRespawnTime() != 0 && temporarySpawn.isInSpawnTime())) {
					for (Integer instanceId : instances) {
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
					if (temporarySpawn.canSpawn() || (startCheck && !template.isNoRespawn() && temporarySpawn.isInSpawnTime())) {
						for (Integer instanceId : instances)
							SpawnEngine.spawnObject(template, instanceId);
					}
				}
			}
		}
	}

	/**
	 * @param spawnTemplate
	 */
	public static void addSpawnGroup(SpawnGroup spawn, int instanceId) {
		temporarySpawns.add(spawn);
		HashSet<Integer> instances = tempSpawnInstanceMap.get(spawn);
		if (instances == null) {
			instances = new HashSet<>();
			tempSpawnInstanceMap.put(spawn, instances);
		}
		instances.add(instanceId);
	}
}
