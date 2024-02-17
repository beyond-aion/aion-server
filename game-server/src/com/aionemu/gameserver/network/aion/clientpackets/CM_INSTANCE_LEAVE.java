package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author xTz
 */
public class CM_INSTANCE_LEAVE extends AionClientPacket {

	public CM_INSTANCE_LEAVE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		// nothing to read
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player.isInInstance()) {
			player.getPosition().getWorldMapInstance().getInstanceHandler().leaveInstance(player);
		}
	}
}
