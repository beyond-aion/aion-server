package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;

/**
 * @author Luno
 */
public class CacheConfig {

	/**
	 * Says whether cache for such things like PlayerCommonData or Appereance etc is cached in {@link WeakCacheMap} or in
	 * {@link SoftCacheMap}
	 */
	@Property(key = "gameserver.cache.softcache", defaultValue = "false")
	public static boolean SOFT_CACHE_MAP;

	/**
	 * If true then whole {@link Player} objects are cached as long as there is memory for them
	 */
	@Property(key = "gameserver.cache.players", defaultValue = "false")
	public static boolean CACHE_PLAYERS;

	/**
	 * If true then whole {@link PlayerCommonData} objects are cached as long as there is memory for them
	 */
	@Property(key = "gameserver.cache.pcd", defaultValue = "false")
	public static boolean CACHE_COMMONDATA;

	/**
	 * If true then whole {@link Account} objects are cached as long as there is memory for them
	 */
	@Property(key = "gameserver.cache.accounts", defaultValue = "false")
	public static boolean CACHE_ACCOUNTS;
}
