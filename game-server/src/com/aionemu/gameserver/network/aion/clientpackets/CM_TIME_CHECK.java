package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AFTER_TIME_CHECK_4_7_5;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TIME_CHECK;

/**
 * I dont know what this packet is doing - probably its ping/pong packet
 * 
 * @author -Nemesiss-
 */
public class CM_TIME_CHECK extends AionClientPacket {

	/**
	 * Nano time / 1000000
	 */
	private int nanoTime;

	/**
	 * Constructs new instance of <tt>CM_VERSION_CHECK</tt> packet
	 * 
	 * @param opcode
	 */
	public CM_TIME_CHECK(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		nanoTime = readD();
	}

	@Override
	protected void runImpl() {
		// int timeNow = (int) (System.nanoTime() / 1000000);
		// int diff = timeNow - nanoTime;
		// System.out.println("CM_TIME_CHECK: " + nanoTime + " =?= " + timeNow + " dif: " + diff);
		AionConnection client = getConnection();
		client.sendPacket(new SM_AFTER_TIME_CHECK_4_7_5()); // don't know what is this doing it is send after this on retail
		client.sendPacket(new SM_TIME_CHECK(nanoTime));
	}
}
