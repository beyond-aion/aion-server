package com.aionemu.gameserver.utils.collections.cachemap;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

/**
 * Base class for {@link WeakCacheMap} and {@link SoftCacheMap}
 * 
 * @author Luno
 * @param <K>
 * @param <V>
 */
abstract class AbstractCacheMap<K, V> implements CacheMap<K, V> {

	private final Logger log;

	protected final String cacheName;
	protected final String valueName;

	/** Map storing references to cached objects */
	protected final Map<K, Reference<V>> cacheMap = new HashMap<K, Reference<V>>();

	protected final ReferenceQueue<V> refQueue = new ReferenceQueue<V>();

	/**
	 * @param cacheName
	 * @param valueName
	 */
	AbstractCacheMap(String cacheName, String valueName, Logger log) {
		this.cacheName = "#CACHE  [" + cacheName + "]#  ";
		this.valueName = valueName;
		this.log = log;
	}

	/** {@inheritDoc} */
	@Override
	public void put(K key, V value) {
		cleanQueue();

		if (cacheMap.containsKey(key))
			throw new IllegalArgumentException("Key: " + key + " already exists in map");

		Reference<V> entry = newReference(key, value, refQueue);

		cacheMap.put(key, entry);

		if (log.isDebugEnabled())
			log.debug(cacheName + " : added " + valueName + " for key: " + key);
	}

	/** {@inheritDoc} */
	@Override
	public V get(K key) {
		cleanQueue();

		Reference<V> reference = cacheMap.get(key);

		if (reference == null)
			return null;

		V res = reference.get();

		if (res != null && log.isDebugEnabled())
			log.debug(cacheName + " : obtained " + valueName + " for key: " + key);

		return res;
	}

	@Override
	public boolean contains(K key) {
		cleanQueue();
		return cacheMap.containsKey(key);
	}

	protected abstract void cleanQueue();

	@Override
	public void remove(K key) {
		cacheMap.remove(key);
	}

	protected abstract Reference<V> newReference(K key, V value, ReferenceQueue<V> queue);
}
