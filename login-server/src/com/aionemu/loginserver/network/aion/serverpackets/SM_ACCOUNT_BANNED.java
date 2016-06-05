package com.aionemu.loginserver.network.aion.serverpackets;

import com.aionemu.loginserver.network.aion.AionServerPacket;
import com.aionemu.loginserver.network.aion.LoginConnection;

/**
 * Sent to notify that the account was banned
 * 
 * @author Neon
 */
public final class SM_ACCOUNT_BANNED extends AionServerPacket {

	public SM_ACCOUNT_BANNED() {
		super(0x09);
	}

	@Override
	protected void writeImpl(LoginConnection con) {
	}
}
