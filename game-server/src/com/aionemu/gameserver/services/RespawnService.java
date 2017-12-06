package com.aionemu.gameserver.services;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.event.EventTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer, Source, xTz
 * @modified Neon
 */
public class RespawnService {

	public static final int IMMEDIATE_DECAY = 2 * 1000;
	public static final int WITH_DROP_DECAY = 5 * 60 * 1000;
	private static final Map<Integer, ScheduledRespawn> pendingRespawns = new ConcurrentHashMap<>();

	/**
	 * Schedules decay (despawn) of the npc with the default delay time. If there is already a decay task, it will be replaced with this one. The task
	 * can then be accessed via {@link NpcController#getTask(TaskId.DECAY)}
	 */
	public static Future<?> scheduleDecayTask(Npc npc) {
		int decayInterval;
		Set<DropItem> drop = DropRegistrationService.getInstance().getCurrentDropMap().get(npc.getObjectId());

		if (drop == null || drop.isEmpty())
			decayInterval = IMMEDIATE_DECAY;
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
	public static ScheduledRespawn scheduleRespawn(VisibleObject visibleObject) {
		SpawnTemplate spawnTemplate = visibleObject.getSpawn();
		if (spawnTemplate == null || spawnTemplate.isNoRespawn())
			return null;
		RespawnTask respawnTask = new RespawnTask(visibleObject);
		Future<?> task = ThreadPoolManager.getInstance().schedule(respawnTask, spawnTemplate.getRespawnTime() * 1000);
		ScheduledRespawn scheduledRespawn = new ScheduledRespawn(task, respawnTask);
		ScheduledRespawn oldScheduledRespawn = pendingRespawns.put(visibleObject.getObjectId(), scheduledRespawn);
		if (oldScheduledRespawn != null)
			LoggerFactory.getLogger(RespawnService.class).warn("Duplicate respawn task initiated for " + visibleObject
				+ " or the previous objectId owner had a pending respawn task but auto released its ID during finalization (see AionObject.finalize())");
		return scheduledRespawn;
	}

	public static boolean hasRespawnTask(int objectId) {
		return pendingRespawns.containsKey(objectId);
	}

	public static void cancelRespawn(VisibleObject object) {
		cancelRespawn(object.getObjectId());
	}

	public static boolean cancelRespawn(int objectId) {
		ScheduledRespawn scheduledRespawn = pendingRespawns.get(objectId);
		if (scheduledRespawn != null) {
			scheduledRespawn.cancel();
			return true;
		}
		return false;
	}

	public static int cancelEventRespawns(EventTemplate eventTemplate) {
		int count = 0;
		for (ScheduledRespawn respawn : pendingRespawns.values()) {
			if (eventTemplate.equals(respawn.getSpawnTemplate().getEventTemplate())) {
				respawn.cancel();
				count++;
			}
		}
		return count;
	}

	private static ScheduledRespawn unregisterRespawnTask(int objectId) {
		ScheduledRespawn respawn = pendingRespawns.remove(objectId);
		if (respawn != null && !World.getInstance().isInWorld(objectId))
			IDFactory.getInstance().releaseId(objectId);
		return respawn;
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
				visibleObject.getController().delete();
			}
		}

	}

	private static class RespawnTask implements Runnable {

		private final SpawnTemplate spawn;
		private final int instanceId;
		private final int despawnedObjectId;

		private RespawnTask(VisibleObject object) {
			this.spawn = object.getSpawn();
			this.instanceId = object.getInstanceId();
			this.despawnedObjectId = object.getObjectId();
		}

		@Override
		public void run() {
			unregister();
			respawn(spawn, instanceId);
		}

		public void unregister() {
			unregisterRespawnTask(despawnedObjectId);
		}

		private void respawn(SpawnTemplate spawnTemplate, int instanceId) {
			if (spawnTemplate.isTemporarySpawn() && !spawnTemplate.getTemporarySpawn().isInSpawnTime())
				return;

			if (!InstanceService.isInstanceExist(spawnTemplate.getWorldId(), instanceId))
				return;

			if (spawnTemplate.hasPool())
				spawnTemplate = spawnTemplate.changeTemplate(instanceId);

			SpawnEngine.spawnObject(spawnTemplate, instanceId);
		}

	}

	private static class ScheduledRespawn {

		private final Future<?> future;
		private final RespawnTask respawnTask;

		private ScheduledRespawn(Future<?> future, RespawnTask respawnTask) {
			this.future = future;
			this.respawnTask = respawnTask;
		}

		public SpawnTemplate getSpawnTemplate() {
			return respawnTask.spawn;
		}

		public void cancel() {
			respawnTask.unregister();
			future.cancel(false);
		}
	}

}
