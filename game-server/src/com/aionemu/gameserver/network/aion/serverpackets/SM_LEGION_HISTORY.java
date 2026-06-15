package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.model.team.legion.LegionHistoryAction.Type;
import com.aionemu.gameserver.model.team.legion.LegionHistoryEntry;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Simple, KID, xTz
 */
public class SM_LEGION_HISTORY extends AionServerPacket {

	private static final int ENTRIES_PER_PAGE = 8;

	private final int totalEntries;
	private final int page;
	private final List<LegionHistoryEntry> pageEntries;
	private final Type type;

	public SM_LEGION_HISTORY(List<LegionHistoryEntry> history, Type type) {
		this(history, 0, type);
	}

	public SM_LEGION_HISTORY(List<LegionHistoryEntry> history, int page, Type type) {
		this.totalEntries = history.size();
		this.page = page;
		this.pageEntries = findEntriesForCurrentPage(history);
		this.type = type;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(totalEntries);
		writeD(page); // current page
		writeD(pageEntries.size());
		for (LegionHistoryEntry entry : pageEntries) {
			writeD(entry.epochSeconds());
			writeC(entry.action().getId());
			writeC(0); // unk
			writeS(entry.name(), 32);
			writeS(entry.description(), 32);
			writeH(0);
		}
		writeH(type.ordinal());
	}

	private List<LegionHistoryEntry> findEntriesForCurrentPage(List<LegionHistoryEntry> legionHistory) {
		int startIndex = page * ENTRIES_PER_PAGE;
		if (startIndex < 0 || startIndex >= legionHistory.size())
			return Collections.emptyList();
		int endIndex = Math.min(startIndex + ENTRIES_PER_PAGE, legionHistory.size());
		return legionHistory.subList(startIndex, endIndex);
	}
}
