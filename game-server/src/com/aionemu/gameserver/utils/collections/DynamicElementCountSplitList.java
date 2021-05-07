package com.aionemu.gameserver.utils.collections;

import java.util.List;
import java.util.function.Function;

/**
 * SplitList implementation that will dynamically determine the partition size based on the actual length of the elements contained in the list.
 * 
 * @author Sykra, Neon
 */
public class DynamicElementCountSplitList<Type> extends SplitList<Type> {

	private final Function<Type, Integer> lengthCalculator;
	private final int maxLength;

	/**
	 * @param listToSplit
	 *          List of elements to split
	 * @param oneTimeSplitOnEmptyData
	 *          true if an empty list should produce one split with an empty list
	 * @param maxLength
	 *          maximum length of one split
	 * @param lengthCalculator
	 *          {@link Function} that will calculate the size length of an element
	 */
	public DynamicElementCountSplitList(List<Type> listToSplit, boolean oneTimeSplitOnEmptyData, int maxLength,
		Function<Type, Integer> lengthCalculator) {
		super(listToSplit, oneTimeSplitOnEmptyData);
		this.maxLength = maxLength;
		if (this.maxLength <= 0)
			throw new IllegalArgumentException("maxLength needs to be larger than 0");
		this.lengthCalculator = lengthCalculator;
	}

	@Override
	protected ListPart<Type> newListPart(int partNo, boolean isLast) {
		return new DynamicElementCountListPart(partNo, isLast);
	}

	@SuppressWarnings("serial")
	private class DynamicElementCountListPart extends ListPart<Type> {

		private int currentLength;

		private DynamicElementCountListPart(int partNo, boolean isLast) {
			super(partNo, isLast);
		}

		@Override
		public boolean add(Type type) {
			if (super.add(type)) {
				currentLength += DynamicElementCountSplitList.this.lengthCalculator.apply(type);
				return true;
			}
			return false;
		}

		@Override
		protected boolean fits(Type element) {
			int elementLength = DynamicElementCountSplitList.this.lengthCalculator.apply(element);
			if (elementLength < 0)
				throw new IllegalStateException("elementLength(" + elementLength + ") cannot be lesser than 0");
			if (elementLength > maxLength)
				throw new IllegalStateException("elementLength(" + elementLength + ") is greater than the maxLength (" + maxLength + ")");
			return elementLength + currentLength <= maxLength;
		}
	}

}
