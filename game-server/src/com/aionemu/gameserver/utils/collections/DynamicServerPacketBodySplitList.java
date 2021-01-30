package com.aionemu.gameserver.utils.collections;

import com.aionemu.gameserver.network.aion.AionServerPacket;

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
		super(listToSplit, oneTimeSplitOnEmptyData, AionServerPacket.MAX_USABLE_PACKET_BODY_SIZE - staticBodyByteSize, byteLengthCalculator);
	}

}
