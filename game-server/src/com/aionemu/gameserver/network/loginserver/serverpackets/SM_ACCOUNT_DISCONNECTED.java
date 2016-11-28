package com.aionemu.gameserver.network.loginserver.serverpackets;

import com.aionemu.gameserver.network.loginserver.LoginServerConnection;
import com.aionemu.gameserver.network.loginserver.LsServerPacket;

/**
 * In this packet GameServer is informing LoginServer that some account is no longer on GameServer [ie was disconencted]
 * 
 * @author -Nemesiss-
 */
public class SM_ACCOUNT_DISCONNECTED extends LsServerPacket {

	/**
	 * AccountId of account that is no longer on GameServer.
	 */
	private final int accountId;

	/**
	 * Constructs new instance of <tt>SM_ACCOUNT_DISCONNECTED </tt> packet.
	 * 
	 * @param accountId
	 *          account id
	 */
	public SM_ACCOUNT_DISCONNECTED(int accountId) {
		super(0x03);
		this.accountId = accountId;
	}

	@Override
	protected void writeImpl(LoginServerConnection con) {
		writeD(accountId);
	}
}
