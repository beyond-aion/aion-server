package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.teleport.TeleportService;

/**
 * Sent by the client when the teleport animation is done and the actual teleport should be executed.
 * 
 * @author Rolandas, Neon
 */
public class CM_TELEPORT_ANIMATION_DONE extends AionClientPacket {

	public CM_TELEPORT_ANIMATION_DONE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
	}

	@Override
	protected void runImpl() {
		TeleportService.executePendingTeleport(getConnection().getActivePlayer());
	}

}
