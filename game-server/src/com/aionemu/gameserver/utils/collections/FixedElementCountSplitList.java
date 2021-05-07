package com.aionemu.gameserver.utils.collections;

import java.util.List;

/**
 * SplitList implementation that will determine the partition length by a fixed max element count
 *
 * @author Sykra, Neon
 */
public class FixedElementCountSplitList<Type> extends SplitList<Type> {

	private final int maxElementCount;

	/**
	 * @param listToSplit
	 *          List of elements to split
	 * @param oneTimeSplitOnEmptyData
	 *          true if an empty list should produce one split with an empty list
	 * @param maxElementCount
	 *          fixed maximum element count used for a partition. It needs to be greater than 0 otherwise an {@link IllegalArgumentException} is thrown
	 */
	public FixedElementCountSplitList(List<Type> listToSplit, boolean oneTimeSplitOnEmptyData, int maxElementCount) {
		super(listToSplit, oneTimeSplitOnEmptyData);
		this.maxElementCount = maxElementCount;
		if (maxElementCount <= 0)
			throw new IllegalArgumentException("maxElementCount needs to be larger than 0");
	}

	@Override
	protected ListPart<Type> newListPart(int partNo, boolean isLast) {
		return new FixedElementCountListPart(partNo, isLast);
	}

	@SuppressWarnings("serial")
	private class FixedElementCountListPart extends ListPart<Type> {

		protected FixedElementCountListPart(int partNo, boolean isLast) {
			super(partNo, isLast);
		}

		@Override
		protected boolean fits(Type element) {
			return size() < maxElementCount;
		}
	}
}
