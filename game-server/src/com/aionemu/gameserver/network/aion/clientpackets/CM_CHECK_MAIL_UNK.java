package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author ginho1
 */
public class CM_CHECK_MAIL_UNK extends AionClientPacket {

	/**
	 * @param opcode
	 * @param state
	 * @param restStates
	 */
	public CM_CHECK_MAIL_UNK(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {

	}

	@Override
	protected void runImpl() {
		// TODO???
	}
}
