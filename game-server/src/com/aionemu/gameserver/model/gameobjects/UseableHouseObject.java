package com.aionemu.gameserver.model.gameobjects;

import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.HouseRegistry;
import com.aionemu.gameserver.model.templates.housing.PlaceableHouseObject;
import com.aionemu.gameserver.world.World;

/**
 * @author Neon
 */
public abstract class UseableHouseObject<T extends PlaceableHouseObject> extends HouseObject<T> {

	private final AtomicInteger usingPlayer = new AtomicInteger();

	private volatile long autoReleaseTimeMs = 0;

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
		return usingPlayer.get() != 0 && autoReleaseTimeMs >= System.currentTimeMillis();
	}

	/**
	 * @return False if the occupant could not be set (another player currently uses the object).
	 */
	public final boolean setOccupant(Player player) {
		return setOccupant(player, 0);
	}

	/**
	 * @return False if the occupant could not be set (another player currently uses the object).
	 */
	public final boolean setOccupant(Player player, int reuseDelayMs) {
		long now = System.currentTimeMillis();
		if (usingPlayer.compareAndSet(0, player.getObjectId()) || usingPlayer.get() == player.getObjectId()) {
			autoReleaseTimeMs = now + reuseDelayMs;
			return true;
		} else if (World.getInstance().findPlayer(usingPlayer.get()) == null) {
			usingPlayer.set(player.getObjectId());
			autoReleaseTimeMs = now + reuseDelayMs;
			return true;
		} else if (reuseDelayMs > 0 && autoReleaseTimeMs < now) {
			usingPlayer.set(player.getObjectId());
			autoReleaseTimeMs = now + reuseDelayMs;
			return true;
		}
		return false;
	}

	/**
	 * @param player
	 * @return True if the using player was released and the object is not occupied anymore.
	 */
	public final boolean releaseOccupant(Player player) {
		if (usingPlayer.compareAndSet(player.getObjectId(), 0)) {
			autoReleaseTimeMs = 0;
			return true;
		}
		return false;
	}

	/**
	 * Unsets the using player without any checks. For internal use only.
	 */
	protected final void releaseOccupant() {
		usingPlayer.set(0);
		autoReleaseTimeMs = 0;
	}
}
