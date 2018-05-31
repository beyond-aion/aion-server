package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;

/**
 * This packet is sent before the client disconnects the player due to being AFK too long. The client does not wait for a response (connection gets
 * immediately closed).
 * 
 * @author Neon
 */
public class CM_DISCONNECT extends AionClientPacket {

	public CM_DISCONNECT(int opcode, Set<AionConnection.State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		readC(); // 0 when client auto closes the connection for inactivity, maybe there are other flags in different cases
	}

	@Override
	protected void runImpl() {
		// no need to do something here, since the character will leave world shortly after the connection is closed
	}
}
