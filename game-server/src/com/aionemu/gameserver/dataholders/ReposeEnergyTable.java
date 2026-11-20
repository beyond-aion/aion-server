package com.aionemu.gameserver.dataholders;

import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

/**
 * Energy of Repose by player level: how much is gained per tick offline, which percentage of gained XP it adds as bonus, and its maximum.
 */
@XmlRootElement(name = "repose_energy_table")
@XmlAccessorType(XmlAccessType.NONE)
public class ReposeEnergyTable {

	@XmlElement(name = "level")
	private List<Row> rows;

	private Row[] rowsByLevel;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		rowsByLevel = new Row[rows.size()];
		for (Row row : rows)
			rowsByLevel[row.level - 1] = row;
		rows = null;
	}

	public long getEnergyPerTick(int level) {
		return getRow(level).energyPerTick;
	}

	public int getUseRatio(int level) {
		return getRow(level).useRatio;
	}

	public long getMaxEnergy(int level) {
		return getRow(level).maxEnergy;
	}

	public int size() {
		return rowsByLevel.length;
	}

	private Row getRow(int level) {
		return rowsByLevel[Math.clamp(level - 1, 0, rowsByLevel.length - 1)];
	}

	@XmlType(name = "repose_energy_row")
	@XmlAccessorType(XmlAccessType.NONE)
	private static class Row {

		@XmlAttribute(name = "lvl", required = true)
		private int level;

		@XmlAttribute(name = "energy_per_tick", required = true)
		private long energyPerTick;

		@XmlAttribute(name = "use_ratio", required = true)
		private int useRatio;

		@XmlAttribute(name = "max_energy", required = true)
		private long maxEnergy;
	}
}
