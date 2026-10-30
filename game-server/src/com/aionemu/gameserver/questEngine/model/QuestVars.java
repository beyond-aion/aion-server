package com.aionemu.gameserver.questEngine.model;

import org.slf4j.LoggerFactory;

/**
 * The progress of a quest. The client and the database know it as a single 32-bit integer, which quests split into six small vars, so they can
 * remember several things at once (e.g. the current step in var 0 and a kill counter in var 1).
 * <p>
 * Each var takes 6 bits, var 0 being the lowest. Five of them fit completely (values 0 to 63), the last one only gets the two bits left over
 * (values 0 to 3):
 *
 * <pre>
 * bit:  31-30 | 29-24 | 23-18 | 17-12 | 11-6 | 5-0
 * var:    5   |   4   |   3   |   2   |  1   |  0
 * </pre>
 *
 * For example, var 0 = 5 and var 4 = 10 give the progress 0x0A000005 (167772165). The client reads conditions and markers from the same bits,
 * so the layout must not change. A quest which needs other field widths (e.g. three counters up to 100) can still work on the whole integer via
 * {@link #getQuestVars()} and {@link #setVar(int)}.
 *
 * @author MrPoke
 */
public class QuestVars {

	private static final int VAR_COUNT = 6;
	private static final int BITS_PER_VAR = 6;
	private static final int VAR_MASK = (1 << BITS_PER_VAR) - 1; // 0b111111, max 63
	private static final int LAST_VAR_BITS = Integer.SIZE - BITS_PER_VAR * (VAR_COUNT - 1); // 32 - 30 = 2
	private static final int LAST_VAR_MASK = (1 << LAST_VAR_BITS) - 1; // 0b11, max 3

	private final int[] vars = new int[VAR_COUNT];

	public QuestVars() {
	}

	/**
	 * @param progress
	 *          the whole progress integer, as stored in the database
	 */
	public QuestVars(int progress) {
		setVar(progress);
	}

	public int getVarById(int id) {
		return vars[id];
	}

	/**
	 * A value which doesn't fit into the var is kept, but spills into the vars above it when packed.
	 */
	public void setVarById(int id, int value) {
		if (value > maxValue(id))
			LoggerFactory.getLogger(QuestVars.class).warn("Out of range value was passed for quest var on index {}", id, new IllegalArgumentException());
		vars[id] = value;
	}

	/**
	 * @return The vars packed into the progress integer which the client and the database know.
	 */
	public int getQuestVars() {
		int progress = 0;
		for (int id = 0; id < VAR_COUNT; id++)
			progress |= vars[id] << shift(id);
		return progress;
	}

	/**
	 * Unpacks the progress integer into the vars.
	 */
	public void setVar(int progress) {
		for (int id = 0; id < VAR_COUNT; id++)
			vars[id] = (progress >>> shift(id)) & maxValue(id); // unsigned shift, since the last var occupies the sign bit
	}

	private static int shift(int id) {
		return id * BITS_PER_VAR;
	}

	private static int maxValue(int id) {
		return id == VAR_COUNT - 1 ? LAST_VAR_MASK : VAR_MASK;
	}
}
