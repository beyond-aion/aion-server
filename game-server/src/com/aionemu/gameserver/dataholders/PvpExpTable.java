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
// PvP kill EXP table: based on the victim's level -> base EXP + acquisition limits.
@XmlAccessorType(XmlAccessType.NONE)
public class PvpExpTable {
	private static final int MILLIS_PER_SECOND = 1000;
	@XmlElement(name = "level")
	private List<Row> rows;
	private int[] exp;
	private int[] maxFromAllUser;
	private int[] reduceAmount;
	private int[] reduceInterval;
	private int[] delayTime;
	private int maxLevel;

	void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		int max = 0;
		for (Row row : rows) {
			max = Math.max(max, row.lvl);
		}
		exp = new int[max + 1];
		maxFromAllUser = new int[max + 1];
		reduceAmount = new int[max + 1];
		reduceInterval = new int[max + 1];
		delayTime = new int[max + 1];
		for (Row row : rows) {
			exp[row.lvl] = row.exp;
			maxFromAllUser[row.lvl] = row.maxFromAllUser;
			reduceAmount[row.lvl] = row.reduceAmount;
			reduceInterval[row.lvl] = row.reduceInterval;
			delayTime[row.lvl] = row.delayTime;
		}
		maxLevel = max;
		rows = null;
	}

	public int getExp(int victimLevel) {
		return valueAt(exp, victimLevel);
	}

	// Exceeding the accumulated PvP EXP limit blocks further EXP gain.
	public int getMaxFromAllUser(int killerLevel) {
		return valueAt(maxFromAllUser, killerLevel);
	}

	// Amount of accumulated PvP EXP removed per interval.
	public int getReduceAmount(int killerLevel) {
		return valueAt(reduceAmount, killerLevel);
	}

	// Interval for removing accumulated PvP EXP.
	public long getReduceIntervalMillis(int killerLevel) {
		return valueAt(reduceInterval, killerLevel) * (long) MILLIS_PER_SECOND;
	}

	// Cooldown for gaining PvP EXP from the same target.
	public long getDelayTimeMillis(int killerLevel) {
		return valueAt(delayTime, killerLevel) * (long) MILLIS_PER_SECOND;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	private int valueAt(int[] values, int level) {
		if (values == null) {
			return 0;
		}
		return values[level < 1 ? 1 : Math.min(level, maxLevel)];
	}

	@XmlType(name = "pvp_exp_row")
	@XmlAccessorType(XmlAccessType.NONE)
	private static class Row {
		@XmlAttribute(name = "lvl")
		private int lvl;
		@XmlAttribute(name = "exp")
		private int exp;
		@XmlAttribute(name = "get_max_from_all_user")
		private int maxFromAllUser;
		@XmlAttribute(name = "get_max_reduce_amount")
		private int reduceAmount;
		@XmlAttribute(name = "get_max_reduce_interval")
		private int reduceInterval;
		@XmlAttribute(name = "delay_time")
		private int delayTime;
	}
}