package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PING_RESPONSE;

/**
 * This packet is sent when player write /ping
 * 
 * @author dragoon112
 */
public class CM_PING_REQUEST extends AionClientPacket {

	/**
	 * Constructs new instance of <tt>CM_PING_REQUEST </tt> packet
	 * 
	 * @param opcode
	 */
	public CM_PING_REQUEST(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		// empty
	}

	@Override
	protected void runImpl() {
		sendPacket(new SM_PING_RESPONSE());
	}
}
