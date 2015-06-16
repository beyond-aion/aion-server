package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
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
	public CM_CHAT_AUTH(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
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
		if (GSConfig.ENABLE_CHAT_SERVER) {
			// this packet is sent sometimes after logout from world
			Player player = getConnection().getActivePlayer();
			if (!player.isInPrison()) {
				ChatServer.getInstance().sendPlayerLoginRequst(player);
			}
		}
	}
}
