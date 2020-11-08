package com.aionemu.gameserver.utils.collections;

import java.util.Collection;

/**
 * ListSplitter that will determine the split size based on a fixed element count.
 * 
 * @author Sykra
 * @param <Type>
 *          Type of the collection element that will be split
 */
public class FixedElementCountSplitter<Type> extends ListSplitter<Type> {

	private final int maxElementCount;
	private int currentElementCount;

	/**
	 * @param objects
	 *          Collection of elements to split
	 * @param oneTimeSplitOnEmptyData
	 *          true if an empty collection should produce one split with an empty list
	 * @param maxElementCount
	 *          fixed maximum element count used for splitting. It needs to be greater than 0 otherwise an {@link IllegalArgumentException} is thrown
	 */
	public FixedElementCountSplitter(Collection<Type> objects, boolean oneTimeSplitOnEmptyData, int maxElementCount) {
		super(objects, oneTimeSplitOnEmptyData);
		this.maxElementCount = maxElementCount;
		if (maxElementCount <= 0)
			throw new IllegalArgumentException("maxElementCount needs to be larger than 0");
	}

	@Override
	public boolean hasSpaceForElement(Type element) {
		if (currentElementCount < maxElementCount) {
			currentElementCount++;
			return true;
		}
		return false;
	}

	@Override
	public void resetState() {
		currentElementCount = 0;
	}
}
