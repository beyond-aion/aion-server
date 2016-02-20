package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.LegionService;

/**
 * @author Simple
 * @modified cura, Neon
 */
public class CM_LEGION_SEND_EMBLEM extends AionClientPacket {

	private int legionId;

	public CM_LEGION_SEND_EMBLEM(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		legionId = readD();
	}

	@Override
	protected void runImpl() {
		Legion legion = LegionService.getInstance().getLegion(legionId);
		if (legion != null)
			LegionService.getInstance().sendEmblemData(getConnection().getActivePlayer(), legion.getLegionEmblem(), legionId, legion.getLegionName());
	}
}
