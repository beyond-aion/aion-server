package com.aionemu.gameserver.utils.collections.cachemap;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a simple map implementation for cache usage.<br>
 * <br>
 * Value may be stored in map really long, but it for sure will be removed if there is low memory (and of course there isn't any strong reference to
 * value object)
 * 
 * @author Luno
 */
class SoftCacheMap<K, V> extends AbstractCacheMap<K, V> implements CacheMap<K, V> {

	private static final Logger log = LoggerFactory.getLogger(SoftCacheMap.class);

	/**
	 * This class is a {@link SoftReference} with additional responsibility of holding key object
	 * 
	 * @author Luno
	 */
	private class SoftEntry extends SoftReference<V> {

		private K key;

		SoftEntry(K key, V referent, ReferenceQueue<? super V> q) {
			super(referent, q);
			this.key = key;
		}

		K getKey() {
			return key;
		}
	}

	SoftCacheMap(String cacheName, String valueName) {
		super(cacheName, valueName, log);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected synchronized void cleanQueue() {
		SoftEntry en = null;
		while ((en = (SoftEntry) refQueue.poll()) != null) {
			K key = en.getKey();
			if (log.isDebugEnabled())
				log.debug(cacheName + " : cleaned up " + valueName + " for key: " + key);
			cacheMap.remove(key);
		}
	}

	@Override
	protected Reference<V> newReference(K key, V value, ReferenceQueue<V> vReferenceQueue) {
		return new SoftEntry(key, value, vReferenceQueue);
	}
}
