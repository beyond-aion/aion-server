package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

/**
 * The most XP a member of a mentor group can gain from one shared reward, by the member's level.
 */
@XmlRootElement(name = "mentee_exp_limit_table")
@XmlAccessorType(XmlAccessType.NONE)
public class MenteeExpLimitTable {

	@XmlElement(name = "level")
	private List<Row> rows;

	private long[] limitsByLevel;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		limitsByLevel = new long[rows.size()];
		for (Row row : rows)
			limitsByLevel[row.level - 1] = row.expLimit;
		rows = null;
	}

	public long getExpLimit(int level) {
		return limitsByLevel[Math.clamp(level - 1, 0, limitsByLevel.length - 1)];
	}

	public int size() {
		return limitsByLevel.length;
	}

	@XmlType(name = "mentee_exp_limit_row")
	@XmlAccessorType(XmlAccessType.NONE)
	private static class Row {

		@XmlAttribute(name = "lvl", required = true)
		private int level;

		@XmlAttribute(name = "exp_limit", required = true)
		private long expLimit;
	}
}
