package com.aionemu.gameserver.services;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.event.EventTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.services.event.Event;
import com.aionemu.gameserver.services.event.EventService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.spawnengine.TemporarySpawnEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer, Source, xTz, Neon
 */
public class RespawnService {

	public static final int IMMEDIATE_DECAY = 2 * 1000;
	public static final int WITH_DROP_DECAY = 5 * 60 * 1000;
	private static final Map<Integer, RespawnTask> pendingRespawns = new ConcurrentHashMap<>();
	private static final Logger log = LoggerFactory.getLogger(RespawnService.class);

	/**
	 * Schedules decay (despawn) of the npc with the default delay time. If there is already a decay task, it will be replaced with this one.
	 */
	public static Future<?> scheduleDecayTask(Npc npc) {
		Set<DropItem> drop = DropRegistrationService.getInstance().getCurrentDropMap().get(npc.getObjectId());
		int decayInterval;
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
			if (spawnTemplate == oldRespawnTask.spawnTemplate) {
				log.warn("Duplicate respawn task initiated for {}", visibleObject, new IllegalStateException());
			} else {
				log.warn(
					"ObjectId {} got released and reassigned while there was a still active respawn task for the old objectId owner.\nOld owner: Npc ID: {}, map ID: {}\nNew owner: {}",
					visibleObject.getObjectId(), oldRespawnTask.spawnTemplate.getNpcId(), oldRespawnTask.spawnTemplate.getWorldId(), visibleObject);
			}
		}
		return respawnTask;
	}

	public static boolean hasRespawnTask(VisibleObject visibleObject) {
		return pendingRespawns.containsKey(visibleObject.getObjectId());
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
		if (respawnTask != null && respawnTask.future != null && respawnTask.spawnTemplate == spawnTemplate) {
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

	public static class RespawnTask implements Runnable {

		private final SpawnTemplate spawnTemplate;
		private final int instanceId;
		private final int oldObjectId;
		private Future<?> future;
		private boolean releaseIdOnUnregister;

		public RespawnTask(VisibleObject object) {
			this.spawnTemplate = object.getSpawn();
			this.instanceId = object.getInstanceId();
			this.oldObjectId = object.getObjectId(); // ID of corpse or already despawned object
		}

		@Override
		public void run() {
			if (tryRegisterOnEventEndTask()) {
				future = null;
				return;
			}
			unregister();
			respawn();
		}

		private boolean tryRegisterOnEventEndTask() {
			if (spawnTemplate.isEventSpawn())
				return false;
			for (Event activeEvent : EventService.getInstance().getActiveEvents()) {
				if (activeEvent.getEventTemplate().getSpawns() == null)
					continue;
				// if a currently active event contains an event spawn with custom="true" for this non-event spawn, we register it for respawn when the event ends
				if (activeEvent.getEventTemplate().getSpawns().getTemplates().stream().anyMatch(m -> m.getMapId() == spawnTemplate.getWorldId() && m.getSpawns().stream().anyMatch(spawn -> spawn.getNpcId() == spawnTemplate.getNpcId() && spawn.isCustom())))
					return activeEvent.addOnEventEndTask(this);
			}
			return false;
		}

		private void respawn() {
			if (!InstanceService.instanceExists(spawnTemplate.getWorldId(), instanceId))
				return;

			VisibleObject respawn = SpawnEngine.spawnObject(spawnTemplate.hasPool() ? spawnTemplate.changeTemplate(instanceId) : spawnTemplate, instanceId);
			if (respawn != null) {
				RiftService.getInstance().updateSpawned(oldObjectId, respawn);
				if (respawn.getSpawn().isTemporarySpawn() && respawn.getObjectId() != oldObjectId)
					TemporarySpawnEngine.unregisterSpawned(oldObjectId);
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
