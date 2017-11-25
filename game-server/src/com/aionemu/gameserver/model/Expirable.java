package com.aionemu.gameserver.model;

import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Mr. Poke
 */
public interface Expirable {

	public int getExpireTime();

	public default int secondsUntilExpiration() {
		return getExpireTime() == 0 ? 0 : getExpireTime() - (int) (System.currentTimeMillis() / 1000);
	}

	public default boolean isExpired() {
		return secondsUntilExpiration() < 0;
	}

	public default void onBeforeExpire(Player player, int remainingMinutes) {
	}

	public void onExpire(Player player);

	public default boolean canExpireNow() {
		return true;
	}

}
