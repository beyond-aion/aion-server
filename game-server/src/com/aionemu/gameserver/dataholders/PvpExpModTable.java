package com.aionemu.gameserver.dataholders;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import java.util.List;

/**
 * @author SVDNESS
 */
// Retail: new PvP EXP logic.
// PvP EXP modifier: killer-victim level difference + victim level bracket.
// Large level gap penalty (low-level anti-gank); the client provides a diff of 1..50.
@XmlAccessorType(XmlAccessType.NONE)
public class PvpExpModTable {
	private static final int BRACKETS = 8;
	@XmlElement(name = "level_diff_mod")
	private List<Row> rows;
	private int[][] mods;

	void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		int maxDiff = 0;
		for (Row row : rows) {
			maxDiff = Math.max(maxDiff, row.diff);
		}
		int[][] built = new int[maxDiff + 1][BRACKETS];
		// Difference 0 -> x1.0.
		for (int b = 0; b < BRACKETS; b++) {
			built[0][b] = 100;
		}
		for (Row row : rows) {
			String[] parts = row.mods.trim().split("\\s+");
			if (parts.length != BRACKETS) {
				throw new IllegalStateException("pvp_exp_mod row diff=" + row.diff + " expected " + BRACKETS + " values, got " + parts.length + ".");
			}
			for (int b = 0; b < BRACKETS; b++) {
				built[row.diff][b] = Integer.parseInt(parts[b]);
			}
		}
		mods = built;
		rows = null;
	}

	// EXP multiplier based on the level difference and victim's level.
	public float getMultiplier(int killerLevel, int victimLevel) {
		if (mods == null) {
			return 1f;
		}
		int diff = killerLevel - victimLevel;
		if (diff < 0) {
			diff = 0;
		} else if (diff >= mods.length) {
			diff = mods.length - 1;
		}
		int bracket = (int) Math.ceil(victimLevel / 10.0) - 1;
		if (bracket < 0) {
			bracket = 0;
		} else if (bracket >= BRACKETS) {
			bracket = BRACKETS - 1;
		}
		return mods[diff][bracket] / 100f;
	}

	public int getMaxLevelDiff() {
		return mods == null ? 0 : mods.length - 1;
	}

	@XmlType(name = "pvp_exp_mod_row")
	@XmlAccessorType(XmlAccessType.NONE)
	private static class Row {
		@XmlAttribute(name = "diff")
		private int diff;
		@XmlAttribute(name = "mods")
		private String mods;
	}
}