package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.model.team.legion.LegionHistory;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Simple, KID, xTz
 */
public class SM_LEGION_TABS extends AionServerPacket {

	private static final int entriesPerPage = 8;
	private final int totalEntries;
	private final int page;
	private final List<LegionHistory> pageEntries;
	private final int tabId;

	public SM_LEGION_TABS(List<LegionHistory> legionHistory, int tabId) {
		this(legionHistory, 0, tabId);
	}

	/**
	 * @param legionHistory - whole history entries for given tab
	 * @param page - current viewed page on this tab
	 * @param tabId - 0 = general legion history, 1 = legion siege reward history, 2 = legion warehouse history
	 */
	public SM_LEGION_TABS(List<LegionHistory> legionHistory, int page, int tabId) {
		this.totalEntries = legionHistory.size();
		this.page = page;
		this.pageEntries = findEntriesForCurrentPage(legionHistory);
		this.tabId = tabId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(totalEntries);
		writeD(page); // current page
		writeD(pageEntries.size());
		for (LegionHistory entry : pageEntries) {
			writeD((int) (entry.getTime().getTime() / 1000));
			writeC(entry.getLegionHistoryType().getId());
			writeC(0); // unk
			writeS(entry.getName(), 32);
			writeS(entry.getDescription(), 32);
			writeH(0);
		}
		writeC(tabId);
		writeC(0);
	}

	private List<LegionHistory> findEntriesForCurrentPage(List<LegionHistory> legionHistory) {
		int startIndex = page * entriesPerPage;
		if (startIndex >= legionHistory.size())
			return Collections.emptyList();
		int endIndex = Math.min(startIndex + entriesPerPage, legionHistory.size());
		return legionHistory.subList(startIndex, endIndex);
	}
}
