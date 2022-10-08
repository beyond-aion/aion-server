package com.aionemu.gameserver.controllers;

import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * This class is for controlling VisibleObjects [players, npc's etc]. Its controlling movement, visibility etc.
 * 
 * @author -Nemesiss-
 */
public abstract class VisibleObjectController<T extends VisibleObject> {

	/**
	 * Object that is controlled by this controller.
	 */
	private T owner;

	/**
	 * Set owner (controller object).
	 * 
	 * @param owner
	 */
	public void setOwner(T owner) {
		this.owner = owner;
	}

	/**
	 * Get owner (controller object).
	 */
	public final T getOwner() {
		return owner;
	}

	/**
	 * Called when controlled object is seeing other VisibleObject.
	 * 
	 * @param object
	 */
	public void see(VisibleObject object) {
	}

	/**
	 * Called when controlled object no longer sees some other VisibleObject.
	 * 
	 * @param object
	 * @param deleteType
	 */
	public void notSee(VisibleObject object, ObjectDeleteAnimation animation) {
	}

	/**
	 * Called when controlled object no longer knows some other VisibleObject.
	 * 
	 * @param object
	 * @param deleteType
	 */
	public void notKnow(VisibleObject object) {
	}

	/**
	 * Despawns the object (if spawned) and deletes it from the world
	 * 
	 * @see World#removeObject(VisibleObject)
	 */
	public final boolean delete() {
		return World.getInstance().removeObject(getOwner());
	}

	/**
	 * Despawns the object (if spawned) and deletes it from the world. If allowed, a respawn task will be scheduled on successful deletion.
	 *
	 * @see World#removeObject(VisibleObject)
	 */
	public final void deleteAndScheduleRespawn() {
		if (delete() && !RespawnService.hasRespawnTask(getOwner()))
			RespawnService.scheduleRespawn(getOwner());
	}

	/**
	 * Despawns the object and deletes it from the world if alive. Otherwise, cancels its respawn task if present.
	 * 
	 * @see #delete()
	 */
	public final void deleteIfAliveOrCancelRespawn() {
		boolean isDead = getOwner() instanceof Creature creature && creature.isDead();
		if (isDead || !delete())
			RespawnService.cancelRespawn(getOwner());
	}

	/**
	 * Called before object is placed into world
	 */
	public void onBeforeSpawn() {
		if (getOwner().getSpawn() != null && getOwner().getSpawn().getStaticId() > 0)
			GeoService.getInstance().spawnPlaceableObject(getOwner().getWorldId(), getOwner().getInstanceId(), getOwner().getSpawn().getStaticId());
	}

	/**
	 * Called after object was placed into world
	 */
	public void onAfterSpawn() {
	}

	/**
	 * Called before object despawns
	 */
	public void onDespawn() {
		if (getOwner().getSpawn() != null && getOwner().getSpawn().getStaticId() > 0)
			GeoService.getInstance().despawnPlaceableObject(getOwner().getWorldId(), getOwner().getInstanceId(), getOwner().getSpawn().getStaticId());
	}

	/**
	 * Called before object gets removed from the world
	 */
	public void onDelete() {
	}
}
