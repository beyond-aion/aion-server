package com.aionemu.gameserver.model;

import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Mr. Poke
 */
public interface Expirable {

	public int expireTime();

	public default int secondsUntilExpiration() {
		return expireTime() == 0 ? 0 : expireTime() - (int) (System.currentTimeMillis() / 1000);
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
