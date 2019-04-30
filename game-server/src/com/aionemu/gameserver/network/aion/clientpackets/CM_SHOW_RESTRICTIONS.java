package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * Received when a player types /restriction in chat. Corresponds with CM_REPORT_PLAYER. The more reports you get, the higher your restriction level
 * rises (see {@link SM_SYSTEM_MESSAGE#STR_MSG_ACCUSE_UPGRADE_LEVEL(int)})
 * 
 * @author Neon
 */
public class CM_SHOW_RESTRICTIONS extends AionClientPacket {

	public CM_SHOW_RESTRICTIONS(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
	}

	@Override
	protected void runImpl() {
		sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_ACCUSE_INFO_NORMAL()); // can be STR_MSG_ACCUSE_INFO_1_LEVEL to STR_MSG_ACCUSE_INFO_4_LEVEL
	}
}
