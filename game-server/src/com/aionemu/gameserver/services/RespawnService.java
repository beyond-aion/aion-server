package com.aionemu.gameserver.services;

import java.util.Set;
import java.util.concurrent.Future;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer, Source, xTz
 * @modified Neon
 */
public class RespawnService {

	public static final int IMMEDIATE_DECAY = 2 * 1000;
	public static final int WITHOUT_DROP_DECAY = (int) (1.5 * 60 * 1000);
	public static final int WITH_DROP_DECAY = 5 * 60 * 1000;

	/**
	 * Schedules decay (despawn) of the npc with the default delay time. If there is already a decay task, it will be replaced with this one. The task
	 * can then be accessed via {@link NpcController#getTask(TaskId.DECAY)}
	 */
	public static Future<?> scheduleDecayTask(Npc npc) {
		int decayInterval;
		Set<DropItem> drop = DropRegistrationService.getInstance().getCurrentDropMap().get(npc.getObjectId());

		if (drop == null)
			decayInterval = IMMEDIATE_DECAY;
		else if (drop.isEmpty())
			decayInterval = WITHOUT_DROP_DECAY;
		else
			decayInterval = WITH_DROP_DECAY;

		return scheduleDecayTask(npc, decayInterval);
	}

	/**
	 * Schedules decay (despawn) of the object with the specified delay. If the object is a creature type and there is already a decay task registered,
	 * it will be replaced with this one.
	 */
	public static Future<?> scheduleDecayTask(VisibleObject visibleObject, long delay) {
		if (delay == 0)
			delay = IMMEDIATE_DECAY; // always delay, to show death animation
		Future<?> task = ThreadPoolManager.getInstance().schedule(new DecayTask(visibleObject.getObjectId()), delay);
		if (visibleObject instanceof Creature)
			((Creature) visibleObject).getController().addTask(TaskId.DECAY, task);
		return task;
	}

	/**
	 * Schedules respawn of the object. Objects without respawn time (like in instances) or without spawn templates will not respawn. If the object is a
	 * creature type and there is already a respawn task registered, it will be replaced with this one.
	 * 
	 * @param visibleObject
	 * @return The task, if the respawn task was initiated, else null.
	 */
	public static Future<?> scheduleRespawnTask(VisibleObject visibleObject) {
		SpawnTemplate spawnTemplate = visibleObject.getSpawn();
		if (spawnTemplate == null)
			return null;
		int respawnTime = spawnTemplate.getRespawnTime();
		if (respawnTime == 0)
			return null;
		int instanceId = visibleObject.getInstanceId();
		Future<?> task = ThreadPoolManager.getInstance().schedule(new RespawnTask(spawnTemplate, instanceId), respawnTime * 1000);
		if (visibleObject instanceof Creature)
			((Creature) visibleObject).getController().addTask(TaskId.RESPAWN, task);
		return task;
	}

	/**
	 * @param spawnTemplate
	 * @param instanceId
	 */
	private static final VisibleObject respawn(SpawnTemplate spawnTemplate, final int instanceId) {
		if (spawnTemplate.isTemporarySpawn() && !spawnTemplate.getTemporarySpawn().canSpawn() && !spawnTemplate.getTemporarySpawn().isInSpawnTime())
			return null;

		int worldId = spawnTemplate.getWorldId();
		boolean instanceExists = InstanceService.isInstanceExist(worldId, instanceId);
		if (spawnTemplate.isNoRespawn() || !instanceExists) {
			return null;
		}

		if (spawnTemplate.hasPool()) {
			spawnTemplate = spawnTemplate.changeTemplate(instanceId);
		}
		return SpawnEngine.spawnObject(spawnTemplate, instanceId);
	}

	private static class DecayTask implements Runnable {

		private final int objectId;

		DecayTask(int objectId) {
			this.objectId = objectId;
		}

		@Override
		public void run() {
			VisibleObject visibleObject = World.getInstance().findVisibleObject(objectId);
			if (visibleObject != null) {
				visibleObject.getController().onDelete();
			}
		}

	}

	private static class RespawnTask implements Runnable {

		private final SpawnTemplate spawn;
		private final int instanceId;

		RespawnTask(SpawnTemplate spawn, int instanceId) {
			this.spawn = spawn;
			this.instanceId = instanceId;
		}

		@Override
		public void run() {
			VisibleObject visibleObject = spawn.getVisibleObject();
			if (visibleObject instanceof Creature) {
				((Creature) visibleObject).getController().cancelTask(TaskId.RESPAWN);
			}
			respawn(spawn, instanceId);
		}

	}

}
