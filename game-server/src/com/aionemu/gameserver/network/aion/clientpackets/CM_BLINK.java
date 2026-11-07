package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.serverpackets.SM_BLINK;

/**
 * Client confirms that it moved itself to the position of the last {@link SM_BLINK}. The server already has the player there, so the confirmation
 * only ends the wait for it.
 */
public class CM_BLINK extends AionClientPacket {

	public CM_BLINK(int opcode, Set<AionConnection.State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
	}

	@Override
	protected void runImpl() {
	}
}
