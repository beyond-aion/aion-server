package com.aionemu.loginserver.network.aion.serverpackets;

import com.aionemu.loginserver.network.aion.AionServerPacket;
import com.aionemu.loginserver.network.aion.LoginConnection;

/**
 * Sent to notify that the account was banned.
 * Player will see a dialog box saying
 * <pre>
 * Your account has been blocked.
 * Your account rights are limited.
 * You can find further information on the official support website (http://support.aionfreetoplay.com).
 * </pre>
 * 
 * @author Neon
 */
public final class SM_ACCOUNT_BANNED extends AionServerPacket {

	public SM_ACCOUNT_BANNED() {
		super(0x02);
	}

	@Override
	protected void writeImpl(LoginConnection con) {
		// maybe some option to specify a custom message (STR_L2AUTH_BLOCK_*)? need retail sniff
	}
}
