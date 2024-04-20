package com.aionemu.gameserver.network.loginserver.serverpackets;

import java.util.List;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.loginserver.LoginServerConnection;
import com.aionemu.gameserver.network.loginserver.LsServerPacket;

/**
 * GameServer packet that sends list of logged in accounts
 * 
 * @author SoulKeeper, Neon
 */
public class SM_ACCOUNT_LIST extends LsServerPacket {

	/**
	 * Map with loaded accounts
	 */
	private final List<AionConnection> accounts;

	/**
	 * constructs new server packet with specified opcode.
	 */
	public SM_ACCOUNT_LIST(List<AionConnection> accounts) {
		super(0x04);
		this.accounts = accounts;
	}

	@Override
	protected void writeImpl(LoginServerConnection con) {
		writeD(accounts.size());
		for (AionConnection ac : accounts)
			writeD(ac.getAccount().getId());
	}
}
