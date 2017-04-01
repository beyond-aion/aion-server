package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MARK_FRIENDLIST;

/**
 * @author xTz, Rolandas
 */
public class CM_MARK_FRIENDLIST extends AionClientPacket {

	public CM_MARK_FRIENDLIST(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		// nothing to read
	}

	@Override
	protected void runImpl() {
		sendPacket(new SM_MARK_FRIENDLIST());
	}
}
