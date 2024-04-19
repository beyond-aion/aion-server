package com.aionemu.gameserver.model.gameobjects;

import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.HouseRegistry;
import com.aionemu.gameserver.model.templates.housing.PlaceableHouseObject;

/**
 * @author Neon
 */
public abstract class UseableHouseObject<T extends PlaceableHouseObject> extends HouseObject<T> {

	private final AtomicInteger usingPlayer = new AtomicInteger();

	public UseableHouseObject(HouseRegistry registry, int objId, int templateId) {
		super(registry, objId, templateId);
	}

	@Override
	public boolean canExpireNow() {
		return !isOccupied();
	}

	/**
	 * @return True if a player currently uses this object and no one else should access it.
	 */
	public boolean isOccupied() {
		return usingPlayer.get() != 0;
	}

	/**
	 * @return True if the player could successful occupy the object or is already occupying it
	 */
	public final boolean setOccupant(Player player) {
		return usingPlayer.compareAndSet(0, player.getObjectId()) || usingPlayer.get() == player.getObjectId();
	}

	/**
	 * @param player
	 * @return True if the using player was released and the object is not occupied anymore.
	 */
	public final boolean releaseOccupant(Player player) {
		return usingPlayer.compareAndSet(player.getObjectId(), 0);
	}

	/**
	 * Unsets the using player without any checks. For internal use only.
	 */
	protected final void releaseOccupant() {
		usingPlayer.set(0);
	}

	public boolean hasUseCooldown() {
		return false;
	}

}
