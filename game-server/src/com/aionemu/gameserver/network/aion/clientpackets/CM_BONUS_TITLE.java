package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author -Enomine-
 */
public class CM_BONUS_TITLE extends AionClientPacket {

	private int bonusTitleId;

	public CM_BONUS_TITLE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		bonusTitleId = readUH();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (bonusTitleId != 0xFFFF)
			if (!player.getTitleList().contains(bonusTitleId))
				return;

		player.getTitleList().setBonusTitle(bonusTitleId);
	}
}
