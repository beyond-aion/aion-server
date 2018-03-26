package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 * This is the base class for all "in-game" objects, that player can interact with, such as: npcs, monsters, players, items.<br>
 * <br>
 * Each AionObject is uniquely identified by objectId.
 * 
 * @author -Nemesiss-, SoulKeeper
 * @modified Neon
 */
public abstract class AionObject {

	/**
	 * Unique id, for all game objects such as: items, players, monsters.
	 */
	private final int objectId;

	public AionObject(int objId) {
		this.objectId = objId;
	}

	/**
	 * Returns unique ObjectId of AionObject
	 * 
	 * @return Int ObjectId
	 */
	public int getObjectId() {
		return objectId;
	}

	@Override
	public final int hashCode() {
		return objectId;
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof AionObject))
			return false;

		if (objectId == 0) // object is a dummy (no unique ID from IDFactory)
			return System.identityHashCode(this) ==  System.identityHashCode(obj);

		return hashCode() == obj.hashCode(); // cheap direct objectId comparison (see above)
	}

	/**
	 * Returns name of the object.<br>
	 * Unique for players, common for NPCs, items, etc
	 * 
	 * @return name of the object
	 */
	public abstract String getName();

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [name=" + getName() + ", objectId=" + objectId + "]";
	}

	@Override
	protected void finalize() throws Throwable {
		if (objectId != 0 && autoReleaseObjectId()) {
			if (!RespawnService.setAutoReleaseId(objectId)) // try to register auto-release after respawn/respawn cancel
				IDFactory.getInstance().releaseId(objectId); // otherwise release ID now
		}
		super.finalize();
	}

	protected boolean autoReleaseObjectId() {
		return false;
	}

}
