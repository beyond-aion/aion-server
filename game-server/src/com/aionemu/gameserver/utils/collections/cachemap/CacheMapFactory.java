package com.aionemu.gameserver.utils.collections.cachemap;

import com.aionemu.gameserver.configs.main.CacheConfig;

/**
 * @author Luno
 */
public class CacheMapFactory {

	/**
	 * Returns new instance of either {@link WeakCacheMap} or {@link SoftCacheMap} depending on {@link CacheConfig#SOFT_CACHE_MAP} setting.
	 * 
	 * @param <K>
	 *          - Type of keys
	 * @param <V>
	 *          - Type of values
	 * @param cacheName
	 *          - The name for this cache map
	 * @param valueName
	 *          - Mnemonic name for values stored in the cache
	 * @return CacheMap<K, V>
	 */
	public static <K, V> CacheMap<K, V> createCacheMap(String cacheName, String valueName) {
		if (CacheConfig.SOFT_CACHE_MAP)
			return createSoftCacheMap(cacheName, valueName);
		else
			return createWeakCacheMap(cacheName, valueName);
	}

	/**
	 * Creates and returns an instance of {@link SoftCacheMap}
	 * 
	 * @param <K>
	 *          - Type of keys
	 * @param <V>
	 *          - Type of values
	 * @param cacheName
	 *          - The name for this cache map
	 * @param valueName
	 *          - Mnemonic name for values stored in the cache
	 * @return CacheMap<K, V>
	 */
	public static <K, V> CacheMap<K, V> createSoftCacheMap(String cacheName, String valueName) {
		return new SoftCacheMap<>(cacheName, valueName);
	}

	/**
	 * Creates and returns an instance of {@link WeakCacheMap}
	 * 
	 * @param <K>
	 *          - Type of keys
	 * @param <V>
	 *          - Type of values
	 * @param cacheName
	 *          - The name for this cache map
	 * @param valueName
	 *          - Mnemonic name for values stored in the cache
	 * @return CacheMap<K, V>
	 */
	public static <K, V> CacheMap<K, V> createWeakCacheMap(String cacheName, String valueName) {
		return new WeakCacheMap<>(cacheName, valueName);
	}
}
