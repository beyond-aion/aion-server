package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RECALLED_BY_OTHER;

/**
 * @author SVDNESS
 */
public class CM_RECALLED_BY_OTHER_ANSWER extends AionClientPacket {

	private int answer;

	public CM_RECALLED_BY_OTHER_ANSWER(int opcode, Set<AionConnection.State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		answer = readUC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		player.getResponseRequester().respond(SM_RECALLED_BY_OTHER.RECALL_REQUEST_ID, answer);
	}
}