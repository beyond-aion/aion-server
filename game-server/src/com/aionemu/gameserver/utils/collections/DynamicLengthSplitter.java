package com.aionemu.gameserver.utils.collections;

import java.util.Collection;
import java.util.function.Function;

/**
 * ListSplitter that will dynamically determine the split size based on the actual length of the elements contained in the collection.
 *
 * @author Sykra
 * @param <Type>
 *          Type of the collection element that will be split
 */
public class DynamicLengthSplitter<Type> extends ListSplitter<Type> {

	private final Function<Type, Integer> lengthCalculator;
	private final int maxLength;
	private int currentLength;

	/**
	 * @param objects
	 *          Collection of elements to split
	 * @param oneTimeSplitOnEmptyData
	 *          true if an empty collection should produce one split with an empty list
	 * @param maxLength
	 *          maximum length of one split
	 * @param lengthCalculator
	 *          {@link Function} that will calculate the size length of an element
	 */
	public DynamicLengthSplitter(Collection<Type> objects, boolean oneTimeSplitOnEmptyData, int maxLength, Function<Type, Integer> lengthCalculator) {
		super(objects, oneTimeSplitOnEmptyData);
		this.maxLength = maxLength;
		if (this.maxLength <= 0)
			throw new IllegalArgumentException("maxLength needs to be larger than 0");
		this.lengthCalculator = lengthCalculator;
	}

	@Override
	public boolean hasSpaceForElement(Type element) {
		int elementLength = lengthCalculator.apply(element);
		if (elementLength < 0)
			throw new IllegalStateException("elementLength(" + elementLength + ") cannot be lesser than 0");
		if (elementLength > maxLength)
			throw new IllegalStateException("elementLength(" + elementLength + ") is greater than the maxLength (" + maxLength + ")");
		if (currentLength + elementLength <= maxLength) {
			currentLength += elementLength;
			return true;
		}
		return false;
	}

	@Override
	public void resetState() {
		currentLength = 0;
	}

}
