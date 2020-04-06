package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

public class CM_HEADING_UPDATE extends AionClientPacket {

	// Client sends this packet after spin effect to update the heading (in that case we already set the heading before we receive this packet)
	// TODO: Find out when else this packet is sent and what or even if we have to answer
	public CM_HEADING_UPDATE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		readC(); // heading
	}

	@Override
	protected void runImpl() {
	}
}
