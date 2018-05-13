package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PONG;

/**
 * @author -Nemesiss-
 * @modified Undertrey, Neon
 */
public class CM_PING extends AionClientPacket {

	public static final int CLIENT_PING_INTERVAL = 180 * 1000; // client sends this packet every 180 seconds

	public CM_PING(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		readH(); // unk
	}

	@Override
	protected void runImpl() {
		getConnection().setLastPingTime(System.currentTimeMillis());
		sendPacket(new SM_PONG());
	}
}
