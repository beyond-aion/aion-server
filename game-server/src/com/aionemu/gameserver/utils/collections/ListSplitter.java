package com.aionemu.gameserver.utils.collections;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javolution.util.FastTable;

/**
 * @author xTz, Rolandas
 */
public class ListSplitter<T> {

	private T[] objects;
	private Class<?> componentType;
	private int splitCount;
	private int nextStartIndex = 0;
	private int length = 0;
	private boolean fetchedFirst = false;
	private boolean calledNext = false;
	private boolean emptyIsOk = false;

	@SuppressWarnings("unchecked")
	public ListSplitter(Collection<T> collection, int splitCount, boolean emptyIsOk) {
		if (collection != null && collection.size() > 0) {
			this.splitCount = splitCount;
			length = collection.size();
			this.objects = collection.toArray((T[]) new Object[length]);
			componentType = objects.getClass().getComponentType();
		}
		this.emptyIsOk = emptyIsOk;
	}

	public List<T> getNext() {
		fetchedFirst = true;
		calledNext = true;

		if (length == 0)
			return new FastTable<>();
		@SuppressWarnings("unchecked")
		T[] subArray = (T[]) Array.newInstance(componentType, Math.min(splitCount, length - nextStartIndex));
		if (subArray.length > 0) {
			System.arraycopy(objects, nextStartIndex, subArray, 0, subArray.length);
			nextStartIndex += subArray.length;
		}
		return Arrays.asList(subArray);
	}

	public int size() {
		return length;
	}

	public boolean isFirst() {
		return !fetchedFirst || calledNext && nextStartIndex - splitCount <= 0;
	}

	public void reset() {
		fetchedFirst = false;
		calledNext = false;
		nextStartIndex = 0;
	}

	/**
	 * Always return true even the array is empty until it was fetched with {@link #getNext()}. Allows to send empty collections as packets using while
	 * loop
	 */
	public boolean hasMore() {
		if (!emptyIsOk && length == 0)
			return false;
		calledNext = false;
		return !fetchedFirst || nextStartIndex < length;
	}

	public boolean isLast() {
		int realNextStart = calledNext ? nextStartIndex - splitCount : nextStartIndex;
		return realNextStart >= length - splitCount;
	}

}
