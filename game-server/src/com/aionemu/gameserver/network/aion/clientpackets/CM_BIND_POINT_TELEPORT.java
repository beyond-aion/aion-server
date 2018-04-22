package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.teleport.BindPointTeleportService;

/**
 * @author ginho1
 */
public class CM_BIND_POINT_TELEPORT extends AionClientPacket {

	private byte action;
	private int locId;
	private long kinah;

	public CM_BIND_POINT_TELEPORT(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		action = readC();// 1 casting, 2 cancel, 3 done
		if (action == 1) {
			locId = readD();
			kinah = readQ();// kinah
		}
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player.isDead())
			return;

		switch (action) {
			case 1:
				BindPointTeleportService.teleport(player, locId, kinah);
				break;
			case 2:
				BindPointTeleportService.cancelTeleport(player, locId);
				break;
		}
	}
}
