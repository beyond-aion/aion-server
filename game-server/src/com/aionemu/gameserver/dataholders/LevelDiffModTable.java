package com.aionemu.gameserver.dataholders;

import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

/**
 * Reward multipliers by the level difference between a player and the defeated creature. Each row holds one column per {@link #getLevelStep()}
 * player levels.
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.NONE)
public abstract class LevelDiffModTable {

	@XmlElement(name = "level_diff_mod")
	private List<Row> rows;

	private int minLevelDiff;
	private int[][] modsByLevelDiff;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		minLevelDiff = rows.stream().mapToInt(row -> row.levelDiff).min().orElseThrow();
		int maxLevelDiff = rows.stream().mapToInt(row -> row.levelDiff).max().orElseThrow();
		modsByLevelDiff = new int[maxLevelDiff - minLevelDiff + 1][];
		for (Row row : rows)
			modsByLevelDiff[row.levelDiff - minLevelDiff] = row.mods;
		rows = null;
	}

	/**
	 * Level differences outside the table use its first or last row.
	 */
	public float getMultiplier(int playerLevel, int targetLevel) {
		int[] mods = modsByLevelDiff[Math.clamp(playerLevel - targetLevel - minLevelDiff, 0, modsByLevelDiff.length - 1)];
		return mods[Math.clamp((playerLevel - 1) / getLevelStep(), 0, mods.length - 1)] / 100f;
	}

	/**
	 * @return Width of one column in player levels; tables differ in how many levels a column covers.
	 */
	protected int getLevelStep() {
		return 10;
	}

	public int size() {
		return modsByLevelDiff.length;
	}

	@XmlType(name = "level_diff_mod_row")
	@XmlAccessorType(XmlAccessType.NONE)
	private static class Row {

		@XmlAttribute(name = "level_diff", required = true)
		private int levelDiff;

		@XmlList
		@XmlAttribute(name = "mods", required = true)
		private int[] mods;
	}
}
