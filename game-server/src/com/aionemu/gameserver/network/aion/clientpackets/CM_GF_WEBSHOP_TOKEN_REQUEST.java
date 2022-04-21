package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GF_WEBSHOP_TOKEN_RESPONSE;

/**
 * Client sends this packet if started with -st parameter.
 * 
 * @author Artur
 */
public class CM_GF_WEBSHOP_TOKEN_REQUEST extends AionClientPacket {

	public CM_GF_WEBSHOP_TOKEN_REQUEST(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
	}

	@Override
	protected void runImpl() {
		sendPacket(new SM_GF_WEBSHOP_TOKEN_RESPONSE("")); // TODO
	}
}
