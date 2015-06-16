package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author Rolandas
 */
public class CM_TIME_CHECK_QUIT extends CM_TIME_CHECK {

	public CM_TIME_CHECK_QUIT(int opcode, State state) {
		super(opcode, state);
	}

}
