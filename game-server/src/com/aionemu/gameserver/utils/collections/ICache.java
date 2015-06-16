package com.aionemu.gameserver.utils.collections;

/**
 * @author Rolandas
 */
@SuppressWarnings({ "rawtypes" })
public interface ICache<K extends Comparable, V> {

	V get(K obj);

	void put(K key, V obj);

	void remove(K key);

	CachePair[] getAll();

	int size();
}
