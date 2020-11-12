package com.aionemu.gameserver.utils.collections;

import java.util.List;
import java.util.function.Function;

/**
 * This class is a more specialized version of the {@link DynamicElementCountSplitList}. It will utilize the maximum server packet body byte size to
 * determine how many elements can be included in a partition. The maximum usable byte size is dynamically determined by the MAX_BODY_SIZE and the
 * given static body byte length.
 * 
 * @author Sykra, Neon
 */
public class DynamicServerPacketBodySplitList<Type> extends DynamicElementCountSplitList<Type> {

	public static final int MAX_BODY_BYTE_SIZE = 8185; // 8192 - 2 (body length) - 2 (opCode) - 1 (staticServerPacketCode) - 2 (opCode flipped bits)

	/**
	 * @param listToSplit
	 *          List of elements to split
	 * @param oneTimeSplitOnEmptyData
	 *          true if an empty list should produce one split with an empty list
	 * @param staticBodyByteSize
	 *          static server packet body size, that will be subtracted from the maximum body size to determine the usable body size in bytes
	 * @param byteLengthCalculator
	 *          {@link Function} that will calculate the byte length of one element
	 */
	public DynamicServerPacketBodySplitList(List<Type> listToSplit, boolean oneTimeSplitOnEmptyData, int staticBodyByteSize,
		Function<Type, Integer> byteLengthCalculator) {
		super(listToSplit, oneTimeSplitOnEmptyData, MAX_BODY_BYTE_SIZE - staticBodyByteSize, byteLengthCalculator);
	}

}
