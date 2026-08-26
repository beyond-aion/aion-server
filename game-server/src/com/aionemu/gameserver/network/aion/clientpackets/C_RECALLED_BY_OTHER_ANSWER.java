package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.serverpackets.S_RECALLED_BY_OTHER;

import java.util.Set;

/**
 * @author SVDNESS
 */

//New packet. Player recall logic.
public class C_RECALLED_BY_OTHER_ANSWER extends AionClientPacket {
	private int answer;

	public C_RECALLED_BY_OTHER_ANSWER(int opcode, Set<AionConnection.State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		answer = readUC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null) {
			return;
		}
		player.getResponseRequester().respond(S_RECALLED_BY_OTHER.RECALL_REQUEST_ID, answer);
	}
}