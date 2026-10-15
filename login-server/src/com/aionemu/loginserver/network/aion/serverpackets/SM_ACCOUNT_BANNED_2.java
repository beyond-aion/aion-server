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

	/**
	 * The client lists an entry per block after the header (a reason bit, whose position selects a STR_L2AUTH_BLOCK_* message, and a text of its
	 * own); none are sent.
	 */
	@Override
	protected void writeImpl(LoginConnection con) {
		writeC(0); // number of entries
	}
}
