package com.aionemu.gameserver.controllers;

import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.world.World;

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
	 * Called when controlled object no longer see some other VisibleObject.
	 * 
	 * @param object
	 * @param deleteType
	 */
	public void notSee(VisibleObject object, ObjectDeleteAnimation animation) {
	}

	/**
	 * Despawns the object (if spawned) and deletes it from the world
	 */
	public final void delete() {
		World.getInstance().removeObject(getOwner());
	}

	/**
	 * Called before object is placed into world
	 */
	public void onBeforeSpawn() {
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
	}

	/**
	 * Called before object gets removed from the world
	 */
	public void onDelete() {
	}
}
