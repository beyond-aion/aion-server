package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.teleport.TeleportService;

/**
 * @author Rolandas
 */
public class CM_HOUSE_TELEPORT_BACK extends AionClientPacket {

	public CM_HOUSE_TELEPORT_BACK(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		// Nothing to read
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		float[] coords = player.getBattleReturnCoords();
		if (coords != null && player.getBattleReturnMap() != 0) {
			TeleportService
				.teleportTo(player, player.getBattleReturnMap(), 1, coords[0], coords[1], coords[2], (byte) 0, TeleportAnimation.FADE_OUT_BEAM);

			player.setBattleReturnCoords(0, null);
		}
	}

}
