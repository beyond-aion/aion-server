package com.aionemu.loginserver.network.aion.serverpackets;

import com.aionemu.loginserver.network.aion.AionServerPacket;
import com.aionemu.loginserver.network.aion.LoginConnection;

/**
 * @author -Nemesiss-
 */
public class SM_AUTH_GG extends AionServerPacket {

	/**
	 * Session Id of this connection
	 */
	private final int sessionId;

	/**
	 * Constructs new instance of <tt>SM_AUTH_GG</tt> packet
	 * 
	 * @param sessionId
	 */
	public SM_AUTH_GG(int sessionId) {
		super(0x0b);

		this.sessionId = sessionId;
	}

	@Override
	protected void writeImpl(LoginConnection con) {
		writeD(sessionId);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0xCD5000); // xor + opcode
		writeD(0); // xor + opcode
		writeD(0x0b << 24); // xor + opcode
		writeD(sessionId ^ 0xCD5000);
		writeB(new byte[3]);
	}
}
