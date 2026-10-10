package com.aionemu.gameserver.dataholders;

import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

/**
 * PvP XP for killing a player of the given level, and the killer's limits for gaining it, by the killer's level.
 * 
 * @author SVDNESS
 */
@XmlRootElement(name = "pvp_exp_table")
@XmlAccessorType(XmlAccessType.NONE)
public class PvpExpTable {

	@XmlElement(name = "level")
	private List<Row> rows;

	private Row[] rowsByLevel;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		rowsByLevel = new Row[rows.size()];
		for (Row row : rows)
			rowsByLevel[row.level - 1] = row;
		rows = null;
	}

	public int getExp(int victimLevel) {
		return getRow(victimLevel).exp;
	}

	/**
	 * @return The accumulated PvP XP above which the player gains no more PvP XP.
	 */
	public int getMaxFromAllUser(int level) {
		return getRow(level).maxFromAllUser;
	}

	public int getReduceAmount(int level) {
		return getRow(level).reduceAmount;
	}

	public long getReduceIntervalMillis(int level) {
		return getRow(level).reduceInterval * 1000L;
	}

	/**
	 * @return The time after killing a player before the killer can gain PvP XP from the same victim again.
	 */
	public long getDelayTimeMillis(int level) {
		return getRow(level).delayTime * 1000L;
	}

	public int getMaxLevel() {
		return rowsByLevel.length;
	}

	private Row getRow(int level) {
		return rowsByLevel[Math.clamp(level - 1, 0, rowsByLevel.length - 1)];
	}

	@XmlType(name = "pvp_exp_row")
	@XmlAccessorType(XmlAccessType.NONE)
	private static class Row {

		@XmlAttribute(name = "lvl", required = true)
		private int level;

		@XmlAttribute(name = "exp", required = true)
		private int exp;

		@XmlAttribute(name = "get_max_from_all_user", required = true)
		private int maxFromAllUser;

		@XmlAttribute(name = "get_max_reduce_amount", required = true)
		private int reduceAmount;

		@XmlAttribute(name = "get_max_reduce_interval", required = true)
		private int reduceInterval;

		@XmlAttribute(name = "delay_time", required = true)
		private int delayTime;
	}
}
