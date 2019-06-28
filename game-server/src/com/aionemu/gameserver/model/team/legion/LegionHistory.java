package com.aionemu.gameserver.model.team.legion;

import java.sql.Timestamp;

/**
 * @author Simple, xTz
 */
public class LegionHistory {

	private LegionHistoryType legionHistoryType;
	private String name;
	private Timestamp time;
	private int tabId;
	private String description;

	public LegionHistory(LegionHistoryType legionHistoryType, String name, Timestamp time, int tabId, String description) {
		this.legionHistoryType = legionHistoryType;
		this.name = name;
		this.time = time;
		this.tabId = tabId;
		this.description = description;
	}

	public LegionHistoryType getLegionHistoryType() {
		return legionHistoryType;
	}

	public String getName() {
		return name;
	}

	public Timestamp getTime() {
		return time;
	}

	public int getTabId() {
		return tabId;
	}

	public String getDescription() {
		return description;
	}
}
