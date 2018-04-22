package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_SEND_EMBLEM;
import com.aionemu.gameserver.services.LegionService;

/**
 * @author cura
 */
public class CM_LEGION_SEND_EMBLEM_INFO extends AionClientPacket {

	private int legionId;

	public CM_LEGION_SEND_EMBLEM_INFO(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		legionId = readD();
	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();
		if (activePlayer == null)
			return;

		Legion legion = LegionService.getInstance().getLegion(legionId);
		if (legion != null)
			sendPacket(new SM_LEGION_SEND_EMBLEM(legionId, legion.getLegionEmblem(), 0, legion.getName())); // send only info without following EMBLEM_DATA packets
	}
}
