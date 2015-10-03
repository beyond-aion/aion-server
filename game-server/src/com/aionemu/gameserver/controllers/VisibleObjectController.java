package com.aionemu.gameserver.controllers;

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
	public T getOwner() {
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
	public void notSee(VisibleObject object, boolean inRange) {

	}

	/**
	 * Removes controlled object from the world.
	 */
	public void delete() {
		/**
		 * despawn object from world.
		 */
		if (getOwner().isSpawned())
			World.getInstance().despawn(getOwner());
		/**
		 * Delete object from World.
		 */

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
	 * Properly despawn object
	 */
	public void onDespawn() {
	}

	/**
	 * This method should be called to make despawn of VisibleObject and delete it from the world
	 */
	public void onDelete() {
		if (getOwner().isInWorld()) {
			this.onDespawn();
			this.delete();
		}
	}
}
