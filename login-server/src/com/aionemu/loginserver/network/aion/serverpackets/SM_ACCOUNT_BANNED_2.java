package com.aionemu.loginserver.network.aion.serverpackets;

import com.aionemu.loginserver.network.aion.AionServerPacket;
import com.aionemu.loginserver.network.aion.LoginConnection;

/**
 * Sent to notify that the account was banned<br>
 * Player will see a dialog box saying
 * 
 * <pre>
 * Your account has been blocked.
 * </pre>
 * 
 * @author Neon
 */
public final class SM_ACCOUNT_BANNED_2 extends AionServerPacket {

	public SM_ACCOUNT_BANNED_2() {
		super(0x09);
	}

	@Override
	protected void writeImpl(LoginConnection con) {
		// maybe some option to specify a custom message (STR_L2AUTH_BLOCK_*)? need retail sniff
	}
}
