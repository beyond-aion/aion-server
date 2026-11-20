package com.aionemu.gameserver.dataholders;

import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

/**
 * Energy of Salvation by player level. Players who gain less XP on their level than expected for their play time on it get bonus points, which add
 * a share of later gained XP.
 */
@XmlRootElement(name = "salvation_energy_table")
@XmlAccessorType(XmlAccessType.NONE)
public class SalvationEnergyTable {

	@XmlElement(name = "level")
	private List<Row> rows;

	private Row[] rowsByLevel;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		rowsByLevel = new Row[rows.size()];
		for (Row row : rows)
			rowsByLevel[row.level - 1] = row;
		rows = null;
	}

	/**
	 * @return The total play time in minutes a player needs before gaining Energy of Salvation.
	 */
	public int getStandardPlayTime(int level) {
		return getRow(level).standardPlayTime;
	}

	public long getStandardExpPerHour(int level) {
		return getRow(level).standardExpPerHour;
	}

	/**
	 * @return The play time in minutes on the current level before gaining Energy of Salvation.
	 */
	public int getCheckStartTime(int level) {
		return getRow(level).checkStartTime;
	}

	/**
	 * @return The minutes between checks whether the player can gain Energy of Salvation.
	 */
	public int getCheckInterval(int level) {
		return getRow(level).checkInterval;
	}

	public long getBonusPointsPerMinute(int level) {
		return getRow(level).bonusPointsPerMinute;
	}

	/**
	 * @return The minutes between two gains of Energy of Salvation.
	 */
	public int getBonusInterval(int level) {
		return getRow(level).bonusInterval;
	}

	public int getUseRatio(int level) {
		return getRow(level).useRatio;
	}

	/**
	 * @return The drop rate bonus in percent while the player can gain Energy of Salvation.
	 */
	public int getDropRatio(int level) {
		return getRow(level).dropRatio;
	}

	public int size() {
		return rowsByLevel.length;
	}

	private Row getRow(int level) {
		return rowsByLevel[Math.clamp(level - 1, 0, rowsByLevel.length - 1)];
	}

	@XmlType(name = "salvation_energy_row")
	@XmlAccessorType(XmlAccessType.NONE)
	private static class Row {

		@XmlAttribute(name = "lvl", required = true)
		private int level;

		@XmlAttribute(name = "standard_playtime", required = true)
		private int standardPlayTime;

		@XmlAttribute(name = "standard_exp_per_hour", required = true)
		private long standardExpPerHour;

		@XmlAttribute(name = "check_start_time", required = true)
		private int checkStartTime;

		@XmlAttribute(name = "check_interval", required = true)
		private int checkInterval;

		@XmlAttribute(name = "bonus_point_per_min", required = true)
		private long bonusPointsPerMinute;

		@XmlAttribute(name = "bonus_interval", required = true)
		private int bonusInterval;

		@XmlAttribute(name = "bonus_use_ratio", required = true)
		private int useRatio;

		@XmlAttribute(name = "drop_ratio", required = true)
		private int dropRatio;
	}
}
