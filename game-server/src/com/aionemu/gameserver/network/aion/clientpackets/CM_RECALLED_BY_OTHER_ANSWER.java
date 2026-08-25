package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.RecallService;
import com.aionemu.gameserver.services.RecallService.CancelReason;

/**
 * Answer to SM_RECALLED_BY_OTHER.
 */
public class CM_RECALLED_BY_OTHER_ANSWER extends AionClientPacket {

	private int answer;

	public CM_RECALLED_BY_OTHER_ANSWER(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		answer = readC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		switch (answer) {
			case 0 -> RecallService.getInstance().accept(player);
			case 1 -> RecallService.getInstance().cancel(player, CancelReason.DECLINED);
			default -> RecallService.getInstance().cancel(player, CancelReason.CANCELLED);
		}
	}
}
