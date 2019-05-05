package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * This packet is sent when a player declines to get revived by another player.
 * 
 * @author Neon
 */
public class CM_REJECT_REVIVE extends AionClientPacket {

	public CM_REJECT_REVIVE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
	}

	@Override
	protected void runImpl() {
	}
}
