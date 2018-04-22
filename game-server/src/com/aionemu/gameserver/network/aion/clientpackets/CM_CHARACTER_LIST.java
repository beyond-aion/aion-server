package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ACCOUNT_PROPERTIES;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CHARACTER_LIST;

/**
 * In this packets aion client is requesting character list.
 * 
 * @author -Nemesiss-
 */
public class CM_CHARACTER_LIST extends AionClientPacket {

	/**
	 * PlayOk2 - we dont care...
	 */
	private int playOk2;

	/**
	 * Constructs new instance of <tt>CM_CHARACTER_LIST </tt> packet.
	 * 
	 * @param opcode
	 */
	public CM_CHARACTER_LIST(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		playOk2 = readD();
	}

	@Override
	protected void runImpl() {
		sendPacket(new SM_ACCOUNT_PROPERTIES());
		sendPacket(new SM_CHARACTER_LIST(playOk2));
	}
}
