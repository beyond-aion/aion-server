package com.aionemu.gameserver.dataholders;

import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

/**
 * Weights for sharing XP within a group, by how many levels a member is below the highest level member.
 */
@XmlRootElement(name = "party_exp_mod_table")
@XmlAccessorType(XmlAccessType.NONE)
public class PartyExpModTable {

	@XmlElement(name = "level_diff_mod")
	private List<Row> rows;

	private int columns;
	private int[] mods;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		columns = rows.getFirst().mods.length;
		mods = new int[rows.size() * columns];
		for (Row row : rows)
			System.arraycopy(row.mods, 0, mods, row.levelDiff * columns, columns);
		rows = null;
	}

	/**
	 * The column is one past the highest level's tens digit, so the index can run into the next row.
	 * 
	 * @return The weight of a member of the given level, or 0 if the member is above the highest level.
	 */
	public float getWeight(int memberLevel, int highestLevel) {
		int levelDiff = highestLevel - memberLevel;
		if (levelDiff < 0)
			return 0;
		int index = Math.min(levelDiff, size() - 1) * columns + highestLevel / 10 + 1;
		return mods[Math.min(index, mods.length - 1)] / 100f;
	}

	public int size() {
		return mods.length / columns;
	}

	@XmlType(name = "party_exp_mod_row")
	@XmlAccessorType(XmlAccessType.NONE)
	private static class Row {

		@XmlAttribute(name = "level_diff", required = true)
		private int levelDiff;

		@XmlList
		@XmlAttribute(name = "mods", required = true)
		private int[] mods;
	}
}
