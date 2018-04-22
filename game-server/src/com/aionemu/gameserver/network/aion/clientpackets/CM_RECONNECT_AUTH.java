package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.loginserver.LoginServer;

/**
 * In this packets aion client is asking for fast reconnection to LoginServer.
 * 
 * @author -Nemesiss-
 */
public class CM_RECONNECT_AUTH extends AionClientPacket {

	public CM_RECONNECT_AUTH(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
	}

	@Override
	protected void runImpl() {
		AionConnection client = getConnection();
		// TODO! check if may reconnect
		LoginServer.getInstance().requestAuthReconnection(client.getAccount().getId(), client);
	}
}
