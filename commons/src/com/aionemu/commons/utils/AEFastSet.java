package com.aionemu.commons.utils;

import java.util.Iterator;
import java.util.Set;

import javolution.util.FastMap;
import javolution.util.FastCollection.Record;

/**
 * @author NB4L1
 */
@SuppressWarnings("unchecked")
public class AEFastSet<E> extends AEFastCollection<E> implements Set<E> {

	private static final Object NULL = new Object();

	private final FastMap<E, Object> map;

	public AEFastSet() {
		map = new FastMap<E, Object>();
	}

	public AEFastSet(int capacity) {
		map = new FastMap<E, Object>(capacity);
	}

	public AEFastSet(Set<? extends E> elements) {
		map = new FastMap<E, Object>(elements.size());

		addAll(elements);
	}

	/*
	 * public AEFastSet<E> setShared(boolean isShared) { map.setShared(isShared); return this; }
	 */

	public boolean isShared() {
		return map.isShared();
	}

	@Override
	public Record head() {
		return map.head();
	}

	@Override
	public Record tail() {
		return map.tail();
	}

	@Override
	public E valueOf(Record record) {
		return ((FastMap.Entry<E, Object>) record).getKey();
	}

	@Override
	public void delete(Record record) {
		map.remove(((FastMap.Entry<E, Object>) record).getKey());
	}

	@Override
	public void delete(Record record, E value) {
		map.remove(value);
	}

	@Override
	public boolean add(E value) {
		return map.put(value, NULL) == null;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public boolean remove(Object o) {
		return map.remove(o) != null;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public String toString() {
		return super.toString() + "-" + map.keySet().toString();
	}
}
