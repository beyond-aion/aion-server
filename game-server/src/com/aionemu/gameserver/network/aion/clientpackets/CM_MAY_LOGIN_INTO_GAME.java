package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MAY_LOGIN_INTO_GAME;

/**
 * In this packets aion client is asking if may login into game [ie start playing].
 * 
 * @author -Nemesiss-
 */
public class CM_MAY_LOGIN_INTO_GAME extends AionClientPacket {

	/**
	 * Constructs new instance of <tt>CM_MAY_LOGIN_INTO_GAME </tt> packet
	 * 
	 * @param opcode
	 */
	public CM_MAY_LOGIN_INTO_GAME(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		// empty
	}

	@Override
	protected void runImpl() {
		AionConnection client = getConnection();
		// TODO! check if may login into game [play time etc]
		client.sendPacket(new SM_MAY_LOGIN_INTO_GAME());
	}
}
