package com.aionemu.loginserver.network.aion.serverpackets;

import com.aionemu.loginserver.network.aion.AionServerPacket;
import com.aionemu.loginserver.network.aion.LoginConnection;
import com.aionemu.loginserver.network.aion.SessionKey;

/**
 * This packet is send to client to update sessionKey [for fast reconnection feature]
 * 
 * @author -Nemesiss-
 */
public class SM_UPDATE_SESSION extends AionServerPacket {

	/**
	 * accountId is part of session key - its used for security purposes
	 */
	private final int accountId;
	/**
	 * loginOk is part of session key - its used for security purposes
	 */
	private final int loginOk;

	/**
	 * Constructs new instance of <tt>SM_UPDATE_SESSION </tt> packet.
	 * 
	 * @param key
	 *          session key
	 */
	public SM_UPDATE_SESSION(SessionKey key) {
		super(0x0c);
		this.accountId = key.accountId;
		this.loginOk = key.loginOk;
	}

	@Override
	protected void writeImpl(LoginConnection con) {
		writeD(accountId);
		writeD(loginOk);
		writeC(0x00);// sysmsg if smth is wrong
	}
}
