package com.aionemu.gameserver.network.loginserver.serverpackets;

import com.aionemu.gameserver.network.loginserver.LoginServerConnection;
import com.aionemu.gameserver.network.loginserver.LsServerPacket;

/**
 * This packet is sended by GameServer when player is requesting fast reconnect to login server. LoginServer in response will send reconectKey.
 * 
 * @author -Nemesiss-
 */
public class SM_ACCOUNT_RECONNECT_KEY extends LsServerPacket {

	/**
	 * AccountId of client that is requested reconnection to LoginServer.
	 */
	private final int accountId;

	/**
	 * Constructs new instance of <tt>SM_ACCOUNT_RECONNECT_KEY </tt> packet.
	 * 
	 * @param accountId
	 *          account identifier.
	 */
	public SM_ACCOUNT_RECONNECT_KEY(int accountId) {
		super(0x02);
		this.accountId = accountId;
	}

	@Override
	protected void writeImpl(LoginServerConnection con) {
		writeD(accountId);
	}
}
