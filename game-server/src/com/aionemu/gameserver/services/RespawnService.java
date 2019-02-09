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
	private static final Map<Integer, RespawnTask> pendingRespawns = new ConcurrentHashMap<>();

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
	public static RespawnTask scheduleRespawn(VisibleObject visibleObject) {
		SpawnTemplate spawnTemplate = visibleObject.getSpawn();
		if (spawnTemplate == null || spawnTemplate.isNoRespawn())
			return null;
		RespawnTask respawnTask = new RespawnTask(visibleObject);
		respawnTask.future = ThreadPoolManager.getInstance().schedule(respawnTask, spawnTemplate.getRespawnTime() * 1000);
		RespawnTask oldRespawnTask = pendingRespawns.put(visibleObject.getObjectId(), respawnTask);
		if (oldRespawnTask != null) { // objectId should not have been in pendingRespawns
			if (visibleObject.getSpawn() == oldRespawnTask.spawnTemplate) {
				LoggerFactory.getLogger(RespawnService.class).warn("Duplicate respawn task initiated for " + visibleObject, new IllegalStateException());
			} else {
				String oldOwnerInfo = "Old owner: Npc ID: " + oldRespawnTask.spawnTemplate.getNpcId() + ", map ID: "
					+ oldRespawnTask.spawnTemplate.getWorldId();
				String newOwnerInfo = "New owner: " + visibleObject;
				LoggerFactory.getLogger(RespawnService.class)
					.warn("ObjectId " + visibleObject.getObjectId()
						+ " got released and reassigned while there was a still active respawn task for the old objectId owner.\n" + oldOwnerInfo + "\n"
						+ newOwnerInfo);
			}
		}
		return respawnTask;
	}

	public static boolean setAutoReleaseId(int objectId) {
		RespawnTask respawn = pendingRespawns.get(objectId);
		if (respawn != null)
			return respawn.setReleaseIdOnCompletion();
		return false;
	}

	public static void cancelRespawn(VisibleObject object) {
		cancelRespawn(object.getObjectId(), object.getSpawn());
	}

	/**
	 * Cancels the respawn for the given objectId only if it also matches the given spawn template (meaning, that this respawn belonged to an npc with
	 * the specified spawn template)
	 */
	public static boolean cancelRespawn(int objectId, SpawnTemplate spawnTemplate) {
		RespawnTask respawnTask = pendingRespawns.get(objectId);
		if (respawnTask != null && respawnTask.spawnTemplate == spawnTemplate) {
			respawnTask.cancel();
			return true;
		}
		return false;
	}

	public static int cancelEventRespawns(EventTemplate eventTemplate) {
		int count = 0;
		for (RespawnTask respawn : pendingRespawns.values()) {
			if (eventTemplate.equals(respawn.spawnTemplate.getEventTemplate())) {
				respawn.cancel();
				count++;
			}
		}
		return count;
	}

	private static class DecayTask implements Runnable {

		private final int objectId;

		private DecayTask(int objectId) {
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

		private final SpawnTemplate spawnTemplate;
		private final int instanceId;
		private final int oldObjectId;
		private Future<?> future;
		private boolean releaseIdOnUnregister;

		private RespawnTask(VisibleObject object) {
			this.spawnTemplate = object.getSpawn();
			this.instanceId = object.getInstanceId();
			this.oldObjectId = object.getObjectId(); // ID of corpse or already despawned object
		}

		@Override
		public void run() {
			unregister();
			respawn();
		}

		private void respawn() {
			if (!InstanceService.isInstanceExist(spawnTemplate.getWorldId(), instanceId))
				return;

			VisibleObject respawn = SpawnEngine.spawnObject(spawnTemplate.hasPool() ? spawnTemplate.changeTemplate(instanceId) : spawnTemplate, instanceId);
			if (respawn != null) {
				RiftService.getInstance().updateSpawned(oldObjectId, respawn);
			}
		}

		private boolean setReleaseIdOnCompletion() {
			synchronized (this) {
				if (this.equals(pendingRespawns.get(oldObjectId))) { // unregistering not yet happened
					releaseIdOnUnregister = true;
					return true;
				}
			}
			return false;
		}

		private void onUnregister() {
			if (releaseIdOnUnregister)
				IDFactory.getInstance().releaseId(oldObjectId);
		}

		public void cancel() {
			unregister();
			future.cancel(false);
		}

		private void unregister() {
			synchronized (this) {
				if (pendingRespawns.remove(oldObjectId, this))
					onUnregister();
			}
		}
	}

}
