package com.aionemu.gameserver.model.team.legion;

/**
 * @author Simple, xTz
 */
public record LegionHistoryEntry(int id, int epochSeconds, LegionHistoryAction action, String name, String description) {

	public LegionHistoryEntry(int id, int epochSeconds, LegionHistoryAction action, String name, String description) {
		this.id = id;
		this.epochSeconds = epochSeconds;
		this.action = action;
		this.name = name.intern();
		this.description = description.intern();
	}
}
