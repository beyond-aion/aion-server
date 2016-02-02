package com.aionemu.gameserver.model.gameobjects;

import javax.annotation.Nullable;

import com.google.common.base.Function;

/**
 * This is the base class for all "in-game" objects, that player can interact with, such as: npcs, monsters, players, items.<br>
 * <br>
 * Each AionObject is uniquely identified by objectId.
 * 
 * @author -Nemesiss-, SoulKeeper
 * @modified Neon
 */
public abstract class AionObject {

	public static Function<AionObject, Integer> OBJECT_TO_ID_TRANSFORMER = new Function<AionObject, Integer>() {

		@Override
		public Integer apply(@Nullable AionObject input) {
			return input != null ? input.getObjectId() : null;
		}
	};

	/**
	 * Unique id, for all game objects such as: items, players, monsters.
	 */
	private Integer objectId;

	public AionObject(Integer objId) {
		this.objectId = objId;
	}

	/**
	 * Returns unique ObjectId of AionObject
	 * 
	 * @return Int ObjectId
	 */
	public Integer getObjectId() {
		return objectId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((objectId == null) ? 0 : objectId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AionObject))
			return false;

		AionObject aionObj = (AionObject) obj;

		if (getObjectId() == null)
			return aionObj.getObjectId() == null && super.equals(obj);

		return getObjectId().equals(aionObj.getObjectId());
	}

	/**
	 * Returns name of the object.<br>
	 * Unique for players, common for NPCs, items, etc
	 * 
	 * @return name of the object
	 */
	public abstract String getName();
}
