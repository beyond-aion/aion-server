package com.aionemu.chatserver.network.aion.clientpackets;

import java.nio.ByteBuffer;

import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.aion.AionConnection;

/**
 * @author Neon
 */
public class CM_PLAYER_INFO extends AbstractClientPacket {

	@SuppressWarnings("unused")
	private int classId, level;
	@SuppressWarnings("unused")
	private byte[] unk;

	/**
	 * Client sends this after authentication and after each teleport.
	 */
	public CM_PLAYER_INFO(ByteBuffer buf, AionConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	@Override
	protected void readImpl() {
		readC(); // 0
		readH(); // 0
		classId = readUC();
		readD(); // 0
		level = readD();
		unk = readB(135);
	}

	@Override
	protected void runImpl() {
		// TODO Find out what other information is sent, maybe handle it if it's useful
	}
}
