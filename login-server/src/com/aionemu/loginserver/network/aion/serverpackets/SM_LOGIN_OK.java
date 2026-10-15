package com.aionemu.loginserver.network.aion.serverpackets;

import com.aionemu.loginserver.network.aion.AionServerPacket;
import com.aionemu.loginserver.network.aion.LoginConnection;
import com.aionemu.loginserver.network.aion.SessionKey;

/**
 * @author -Nemesiss-
 */
public class SM_LOGIN_OK extends AionServerPacket {

	/**
	 * accountId is part of session key - its used for security purposes
	 */
	private final int accountId;
	/**
	 * loginOk is part of session key - its used for security purposes
	 */
	private final int loginOk;

	/**
	 * Constructs new instance of <tt>SM_LOGIN_OK</tt> packet.
	 * 
	 * @param key
	 *          session key
	 */
	public SM_LOGIN_OK(SessionKey key) {
		super(0x03);
		this.accountId = key.accountId;
		this.loginOk = key.loginOk;
	}

	@Override
	protected void writeImpl(LoginConnection con) {
		writeD(accountId);
		writeD(loginOk);
		writeB(new byte[20]); // not used by the 4.8 client (it stores the last three ints, but never reads them)
		writeD(0); // penalty flags: 0x10 shows the internet cafe complaint penalty (STR_BLOCK_PC_ROOM_COMPLAIN2)
		writeB(new byte[16]); // not read by the 4.8 client
		writeD(0); // 1 shows the fatigue warning (STR_AUTH_WARNING_SNDA_FATIGUE) instead of requesting the server list
	}
}
