package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.LegionHistory;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_TABS;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Simple, xTz
 */
public class CM_LEGION_TABS extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_LEGION_TABS.class);

	private int page;
	private int tab;

	public CM_LEGION_TABS(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		page = readD();
		tab = readUC();
	}

	@Override
	@SuppressWarnings("fallthrough")
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();

		if (activePlayer.getLegion() != null) {
			// page amount is limited to 16
			if (page > 16)
				return;

			switch (tab) {
				case 1: // Reward tab
					if (!activePlayer.getLegionMember().isBrigadeGeneral())
						break;
				case 0: // legion history
				case 2: // legion WH history
					List<LegionHistory> history = activePlayer.getLegion().getLegionHistoryByTabId(tab);
					if (!history.isEmpty())
						PacketSendUtility.sendPacket(activePlayer, new SM_LEGION_TABS(history, page, tab));
					break;
			}
		} else
			log.warn("Player " + activePlayer.getName() + " was requested null legion");
	}
}
