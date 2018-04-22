package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author ginho1
 */
public class CM_CHECK_MAIL_LIST extends AionClientPacket {

	public boolean expressOnly;

	/**
	 * @param opcode
	 * @param state
	 * @param restStates
	 */
	public CM_CHECK_MAIL_LIST(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		expressOnly = readC() == 1;

	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		player.getMailbox().sendMailList(expressOnly);
	}

}
