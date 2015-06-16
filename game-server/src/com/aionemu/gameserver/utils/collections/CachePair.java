package com.aionemu.gameserver.utils.collections;

/**
 * @author Rolandas
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CachePair<K extends Comparable, V> implements Comparable<CachePair> {

	public CachePair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K key;
	public V value;

	public boolean equals(Object obj) {
		if (obj instanceof CachePair) {
			CachePair p = (CachePair) obj;
			return key.equals(p.key) && value.equals(p.value);
		}
		return false;
	}

	public int compareTo(CachePair p) {
		int v = key.compareTo(p.key);
		if (v == 0 && p.value instanceof Comparable)
			return ((Comparable) value).compareTo(p.value);
		return v;
	}

	@Override
	public int hashCode() {
		int result = key.hashCode();
		result = 37 * result + value.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return key + ": " + value;
	}
	
}
