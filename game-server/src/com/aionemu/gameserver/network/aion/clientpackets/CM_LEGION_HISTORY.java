package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.LegionHistoryAction.Type;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_HISTORY;

/**
 * @author Simple, xTz, Sykra
 */
public class CM_LEGION_HISTORY extends AionClientPacket {

	private int page;
	private Type type;

	public CM_LEGION_HISTORY(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		page = readD();
		type = Type.values()[readUC()];
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player.getLegion() == null)
			return;
		if (type == Type.REWARD && !player.getLegionMember().isBrigadeGeneral())
			return;
		sendPacket(new SM_LEGION_HISTORY(player.getLegion().getHistory(type), page, type));
	}
}
