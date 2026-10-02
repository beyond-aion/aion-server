package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

/**
 * PvP XP multipliers by the level difference between killer and victim. Each row holds one column per ten killer levels.
 * 
 * @author SVDNESS
 */
@XmlRootElement(name = "pvp_exp_mod_table")
@XmlAccessorType(XmlAccessType.NONE)
public class PvpExpModTable {

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
	public float getMultiplier(int killerLevel, int victimLevel) {
		int[] mods = modsByLevelDiff[Math.clamp(killerLevel - victimLevel - minLevelDiff, 0, modsByLevelDiff.length - 1)];
		return mods[Math.clamp((killerLevel - 1) / 10, 0, mods.length - 1)] / 100f;
	}

	public int size() {
		return modsByLevelDiff.length;
	}

	@XmlType(name = "pvp_exp_mod_row")
	@XmlAccessorType(XmlAccessType.NONE)
	private static class Row {

		@XmlAttribute(name = "level_diff", required = true)
		private int levelDiff;

		@XmlList
		@XmlAttribute(name = "mods", required = true)
		private int[] mods;
	}
}
