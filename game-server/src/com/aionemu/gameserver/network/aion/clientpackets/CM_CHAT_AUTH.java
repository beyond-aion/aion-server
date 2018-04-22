package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.chatserver.ChatServer;

/**
 * Client sends this only once.
 * 
 * @author Luno
 */
public class CM_CHAT_AUTH extends AionClientPacket {

	/**
	 * Constructor
	 * 
	 * @param opcode
	 */
	public CM_CHAT_AUTH(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		@SuppressWarnings("unused")
		int objectId = readD(); // lol NC
		@SuppressWarnings("unused")
		byte[] macAddress = readB(6);
	}

	@Override
	protected void runImpl() {
		ChatServer.getInstance().sendPlayerLoginRequest(getConnection().getActivePlayer());
	}
}
