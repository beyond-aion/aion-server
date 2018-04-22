package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FRIEND_LIST;

/**
 * Send when the client requests the friendlist
 * 
 * @author Ben
 */
public class CM_SHOW_FRIENDLIST extends AionClientPacket {

	public CM_SHOW_FRIENDLIST(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
	}

	@Override
	protected void runImpl() {
		sendPacket(new SM_FRIEND_LIST());
	}

}
