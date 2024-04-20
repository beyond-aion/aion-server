package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author Nemiroff, cura
 */
public class CM_TITLE_SET extends AionClientPacket {

	private int titleId;

	public CM_TITLE_SET(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		titleId = readUH();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (titleId != 0xFFFF)
			if (!player.getTitleList().contains(titleId))
				return;

		player.getTitleList().setDisplayTitle(titleId);
	}
}
