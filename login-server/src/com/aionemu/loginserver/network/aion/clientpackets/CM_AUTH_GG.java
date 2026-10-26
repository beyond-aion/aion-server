package com.aionemu.loginserver.network.aion.clientpackets;

import java.nio.ByteBuffer;

import com.aionemu.loginserver.network.aion.AionAuthResponse;
import com.aionemu.loginserver.network.aion.AionClientPacket;
import com.aionemu.loginserver.network.aion.LoginConnection;
import com.aionemu.loginserver.network.aion.LoginConnection.State;
import com.aionemu.loginserver.network.aion.serverpackets.SM_AUTH_GG;
import com.aionemu.loginserver.network.aion.serverpackets.SM_LOGIN_FAIL;

/**
 * @author -Nemesiss-
 */
public class CM_AUTH_GG extends AionClientPacket {

	/**
	 * session id - its should match sessionId that was send in Init packet.
	 */
	private int sessionId;

	/**
	 * Constructs new instance of <tt>CM_AUTH_GG</tt> packet.
	 * 
	 * @param buf
	 * @param client
	 */
	public CM_AUTH_GG(ByteBuffer buf, LoginConnection client, int opCode) {
		super(buf, client, opCode);
	}

	@Override
	protected void readFields() {
		sessionId = readD();
		readB(16); // zeros in the 4.8 client
	}

	@Override
	protected void runImpl() {
		LoginConnection con = getConnection();
		if (con.getSessionId() == sessionId) {
			con.setState(State.AUTHED_GG);
			con.sendPacket(new SM_AUTH_GG());
		} else {
			con.close(new SM_LOGIN_FAIL(AionAuthResponse.STR_L2AUTH_S_SYSTEM_ERROR)); // Session id is not ok, notify and disconnect client
		}
	}
}
