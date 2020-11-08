package com.aionemu.gameserver.utils.collections;

import java.util.*;

/**
 * Generic list splitter class based on the {@link Iterator}-Interface. This class handles the splitting of a given collection into multiple
 * sublists. The determination if an element can be included in the current iteration must be handled in subclasses.
 *
 * @param <Type>
 *          Type of the collection element that will be split
 * @author xTz, Rolandas, Sykra
 */
public abstract class ListSplitter<Type> implements Iterator<List<Type>> {

	private final List<Type> data;
	private final boolean oneTimeSplitOnEmptyData;
	private int splitCount;
	private int currentIndex;

	/**
	 * @param objects
	 *          Collection of elements to split
	 * @param oneTimeSplitOnEmptyData
	 *          true if an empty collection should produce one split with an empty list
	 */
	public ListSplitter(Collection<Type> objects, boolean oneTimeSplitOnEmptyData) {
		if (objects == null || objects.isEmpty())
			data = Collections.emptyList();
		else
			data = new ArrayList<>(objects);
		this.oneTimeSplitOnEmptyData = oneTimeSplitOnEmptyData;
	}

	/**
	 * This method checks whether the passed in element can be included in the current split iteration in {@link #next()}. It's possible that this
	 * method can create or modify a cross-element state.
	 * 
	 * @param element
	 *          Element that need to be checked
	 * @return true, if the element should be included in the current split. false will include the element in the next split iteration
	 */
	public abstract boolean hasSpaceForElement(Type element);

	/**
	 * This method should clear any state created by {@link #hasSpaceForElement(Object)}.
	 */
	public abstract void resetState();

	@Override
	public synchronized boolean hasNext() {
		if (shouldSplitEmptyList())
			return splitCount == 0;
		return currentIndex != data.size();
	}

	@Override
	public synchronized List<Type> next() {
		if (shouldSplitEmptyList()) {
			if (splitCount == 0) {
				splitCount++;
				resetState();
				return Collections.emptyList();
			} else {
				throw new NoSuchElementException("cannot split empty list more than one time");
			}
		}

		if (currentIndex == data.size())
			throw new NoSuchElementException("reached end of collection - no more data available");

		List<Type> splitElements = new ArrayList<>();
		for (int i = currentIndex; i < data.size(); i++) {
			Type element = data.get(i);
			if (hasSpaceForElement(element)) {
				splitElements.add(element);
				currentIndex++;
			} else {
				break;
			}
		}
		resetState();
		splitCount++;
		return splitElements;
	}

	private boolean shouldSplitEmptyList() {
		return data.isEmpty() && oneTimeSplitOnEmptyData;
	}

	public int getSplitCount() {
		return splitCount;
	}

	public boolean hasNotBeenSplit() {
		return splitCount == 0;
	}

	public boolean isFirstSplit() {
		return splitCount == 1;
	}

	public boolean isLastSplit() {
		if (shouldSplitEmptyList())
			return splitCount == 1;
		return currentIndex == data.size();
	}

}
