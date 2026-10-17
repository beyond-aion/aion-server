package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author xavier Packet sent by client when it starts the 10 second countdown before quitting the game
 */
public class CM_MAY_QUIT extends AionClientPacket {

	/**
	 * @param opcode
	 */
	public CM_MAY_QUIT(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		// empty
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player != null)
			player.setQuitCountdownStartMillis(System.currentTimeMillis());
	}

}
