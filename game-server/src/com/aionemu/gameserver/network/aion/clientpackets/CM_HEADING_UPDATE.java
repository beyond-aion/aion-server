package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

public class CM_HEADING_UPDATE extends AionClientPacket {

	// TODO: Client sends this packet after spin effect. Find out other states and what to answer
	public CM_HEADING_UPDATE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		// heading = readC(); // we already set the heading 
	}

	@Override
	protected void runImpl() {
	}
}
