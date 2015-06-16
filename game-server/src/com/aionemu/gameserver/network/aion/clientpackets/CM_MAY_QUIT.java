package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author xavier Packet sent by client when player may quit game in 10 seconds
 */
public class CM_MAY_QUIT extends AionClientPacket {

	/**
	 * @param opcode
	 */
	public CM_MAY_QUIT(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aionemu.commons.network.packet.BaseClientPacket#readImpl()
	 */
	@Override
	protected void readImpl() {
		// empty
	}

	/*
	 * (non-Javadoc)
	 * @see com.aionemu.commons.network.packet.BaseClientPacket#runImpl()
	 */
	@Override
	protected void runImpl() {
		// Nothing to do
	}

}
