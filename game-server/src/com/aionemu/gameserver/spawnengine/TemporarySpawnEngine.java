package com.aionemu.gameserver.spawnengine;

import java.util.HashSet;

import javolution.util.FastList;
import javolution.util.FastMap;

import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup2;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.TemporarySpawn;

/**
 * @author xTz
 */
public class TemporarySpawnEngine {

	private static final FastList<SpawnGroup2> temporarySpawns = new FastList<SpawnGroup2>();
	private static final FastMap<SpawnGroup2, HashSet<Integer>> tempSpawnInstanceMap = new FastMap<SpawnGroup2, HashSet<Integer>>();

	public static void spawnAll() {
		spawn(true);
	}

	public static void onHourChange() {
		despawn();
		spawn(false);
	}

	private static void despawn() {
		for (SpawnGroup2 spawn : temporarySpawns) {
			for (SpawnTemplate template : spawn.getSpawnTemplates()) {
				if (template.getTemporarySpawn().canDespawn()) {
					VisibleObject object = template.getVisibleObject();
					if (object == null) {
						continue;
					}
					if (object instanceof Npc) {
						Npc npc = (Npc) object;
						if (!npc.getLifeStats().isAlreadyDead() && template.hasPool()) {
							spawn.setTemplateUse(npc.getInstanceId(), template, false);
						}
						npc.getController().cancelTask(TaskId.RESPAWN);
					}
					if (object.isSpawned()) {
						object.getController().onDelete();
					}
				}
			}
		}
	}

	private static void spawn(boolean startCheck) {
		for (SpawnGroup2 spawn : temporarySpawns) {
			HashSet<Integer> instances = tempSpawnInstanceMap.get(spawn);
			if (spawn.hasPool()) {
				TemporarySpawn temporarySpawn = spawn.geTemporarySpawn();
				if (temporarySpawn.canSpawn() || (startCheck && spawn.getRespawnTime() != 0 && temporarySpawn.isInSpawnTime())) {
					for (Integer instanceId : instances) {
						spawn.resetTemplates(instanceId);
						for (int pool = 0; pool < spawn.getPool(); pool++) {
							SpawnTemplate template = spawn.getRndTemplate(instanceId);
							SpawnEngine.spawnObject(template, instanceId);
						}
					}
				}
			}
			else {
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
	public static void addSpawnGroup(SpawnGroup2 spawn, int instanceId) {
		temporarySpawns.add(spawn);
		HashSet<Integer> instances = tempSpawnInstanceMap.get(spawn);
		if (instances == null) {
			instances = new HashSet<Integer>();
			tempSpawnInstanceMap.put(spawn, instances);
		}
		instances.add(instanceId);
	}
}
