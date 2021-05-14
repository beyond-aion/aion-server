package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.LegionHistory;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_TABS;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Simple, xTz, Sykra
 */
public class CM_LEGION_TABS extends AionClientPacket {

	private int page;
	private int tabId;

	public CM_LEGION_TABS(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		page = readD();
		tabId = readUC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player.getLegion() == null)
			return;
		// invalid tab ids
		if (tabId < 0 || tabId > 2)
			return;
		// restrict legion reward tab to brigade general
		if (tabId == 1 && !player.getLegionMember().isBrigadeGeneral())
			return;
		List<LegionHistory> history = player.getLegion().getLegionHistoryByTabId(tabId);
		PacketSendUtility.sendPacket(player, new SM_LEGION_TABS(history, page, tabId));
	}
}
