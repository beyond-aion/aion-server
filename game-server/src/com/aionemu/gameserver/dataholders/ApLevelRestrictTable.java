package com.aionemu.gameserver.dataholders;

import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

/**
 * The most AP a player of a level can have. Gains stop at this value.
 */
@XmlRootElement(name = "ap_level_restricts")
@XmlAccessorType(XmlAccessType.NONE)
public class ApLevelRestrictTable {

	@XmlElement(name = "level")
	private List<Row> rows;

	private long[] maxApByLevel;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		maxApByLevel = new long[rows.size()];
		for (Row row : rows)
			maxApByLevel[row.level - 1] = row.maxAp;
		rows = null;
	}

	public long getMaxAp(int level) {
		return maxApByLevel[Math.clamp(level - 1, 0, maxApByLevel.length - 1)];
	}

	public int size() {
		return maxApByLevel.length;
	}

	@XmlType(name = "ap_level_restrict_row")
	@XmlAccessorType(XmlAccessType.NONE)
	private static class Row {

		@XmlAttribute(name = "lvl", required = true)
		private int level;

		@XmlAttribute(name = "ap_max", required = true)
		private long maxAp;
	}
}
