package com.aionemu.gameserver.questEngine.model;

import org.slf4j.LoggerFactory;

/**
 * @author MrPoke
 */
public class QuestVars {

	private static final int VAR_COUNT = 6;
	private static final int VAR_BITS = 6;
	private static final int VAR_MASK = (1 << VAR_BITS) - 1;
	/** The six vars don't fit into the stored integer, so the last one only has the two bits left above the other five */
	private static final int LAST_VAR_MASK = (1 << (Integer.SIZE - VAR_BITS * (VAR_COUNT - 1))) - 1;

	private int[] questVars = new int[VAR_COUNT];

	public QuestVars() {
	}

	public QuestVars(int var) {
		setVar(var);
	}

	/**
	 * @param id
	 * @return Quest var by id.
	 */
	public int getVarById(int id) {
		return questVars[id];
	}

	/**
	 * @param id
	 * @param var
	 */
	public void setVarById(int id, int var) {
		if (var > getMaxValue(id))
			LoggerFactory.getLogger(QuestVars.class).warn("Out of range value was passed for quest var on index " + id, new IllegalArgumentException());
		questVars[id] = var;
	}

	private static int getMaxValue(int id) {
		return id == VAR_COUNT - 1 ? LAST_VAR_MASK : VAR_MASK;
	}

	/**
	 * @return int value of all values, stored in the array. Representation: Sum(value_on_index_i * 64^i)
	 */
	public int getQuestVars() {
		int var = 0;
		for (int i = VAR_COUNT - 1; i >= 0; i--) {
			var <<= VAR_BITS;
			var |= questVars[i];
		}
		return var;
	}

	/**
	 * Fill the array with values, based on
	 *
	 * @param int
	 *          value, represented like above
	 */
	public void setVar(int var) {
		for (int i = 0; i < VAR_COUNT; i++) {
			questVars[i] = var & VAR_MASK;
			var >>>= VAR_BITS; // unsigned, the last var occupies the sign bit
		}
	}
}
