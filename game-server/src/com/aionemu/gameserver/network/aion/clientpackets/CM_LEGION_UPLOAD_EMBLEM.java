package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.LegionService;

/**
 * @author Simple
 */
public class CM_LEGION_UPLOAD_EMBLEM extends AionClientPacket {

	/** Emblem related information **/
	private byte[] data;

	/**
	 * @param opcode
	 */
	public CM_LEGION_UPLOAD_EMBLEM(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		data = readB(readD());
	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();
		if (activePlayer == null)
			return;

		if (data != null && data.length > 0) {
			LegionService.getInstance().uploadEmblemData(activePlayer, data);
		}
	}
}
