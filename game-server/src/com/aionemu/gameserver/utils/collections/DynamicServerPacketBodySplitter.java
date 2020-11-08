package com.aionemu.gameserver.utils.collections;

import java.util.Collection;
import java.util.function.Function;

/**
 * This class is a more specialized version of the {@link DynamicLengthSplitter}. It will utilize the maximum server packet body byte size to
 * determine
 * how many elements can be included in one split. The maximum usable byte size is dynamically determined by the MAX_BODY_SIZE and the given
 * static body byte length.
 * 
 * @author Sykra
 * @param <Type>
 *          Type of the collection element that will be split
 */
public class DynamicServerPacketBodySplitter<Type> extends DynamicLengthSplitter<Type> {

	public static final int MAX_BODY_BYTE_SIZE = 8185;

	/**
	 * @param objects
	 *          Collection of elements to split
	 * @param oneTimeSplitOnEmptyData
	 *          true if an empty collection should produce one split with an empty list
	 * @param staticBodyByteSize
	 *          static server packet body size, that will be subtracted from the maximum body size to determine the usable body size in bytes
	 * @param byteLengthCalculator
	 *          {@link Function} that will calculate the byte length of one element
	 */
	public DynamicServerPacketBodySplitter(Collection<Type> objects, boolean oneTimeSplitOnEmptyData, int staticBodyByteSize,
		Function<Type, Integer> byteLengthCalculator) {
		super(objects, oneTimeSplitOnEmptyData, MAX_BODY_BYTE_SIZE - staticBodyByteSize, byteLengthCalculator);
	}

}
