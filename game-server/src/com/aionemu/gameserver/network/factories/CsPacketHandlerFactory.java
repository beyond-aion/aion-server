package com.aionemu.gameserver.network.factories;

import com.aionemu.gameserver.network.chatserver.ChatServerConnection.State;
import com.aionemu.gameserver.network.chatserver.CsClientPacket;
import com.aionemu.gameserver.network.chatserver.CsPacketHandler;
import com.aionemu.gameserver.network.chatserver.clientpackets.CM_CS_AUTH_RESPONSE;
import com.aionemu.gameserver.network.chatserver.clientpackets.CM_CS_PLAYER_AUTH_RESPONSE;

/**
 * @author ATracer
 */
public class CsPacketHandlerFactory {

	private CsPacketHandler handler = new CsPacketHandler();

	/**
	 * @param injector
	 */
	public CsPacketHandlerFactory() {
		addPacket(new CM_CS_AUTH_RESPONSE(0x00), State.CONNECTED);
		addPacket(new CM_CS_PLAYER_AUTH_RESPONSE(0x01), State.AUTHED);
	}

	/**
	 * @param prototype
	 * @param states
	 */
	private void addPacket(CsClientPacket prototype, State... states) {
		handler.addPacketPrototype(prototype, states);
	}

	/**
	 * @return handler
	 */
	public CsPacketHandler getPacketHandler() {
		return handler;
	}
}
