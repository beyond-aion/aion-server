package com.aionemu.loginserver.network.aion.serverpackets;

import com.aionemu.loginserver.network.aion.AionServerPacket;
import com.aionemu.loginserver.network.aion.LoginConnection;

/**
 * Sent to notify that the account was banned. Player will see a dialog box saying "Your account has been blocked.", followed by the message of
 * the reason or the flag, if one is given.
 * 
 * @author Neon
 */
public final class SM_ACCOUNT_BANNED extends AionServerPacket {

	private final int reason;
	private final int flags;

	/**
	 * Shows only "Your account has been blocked."
	 */
	public SM_ACCOUNT_BANNED() {
		this(0, 0);
	}

	/**
	 * @param reason
	 *          code of a STR_L2AUTH_BLOCK_* message (1 STR_L2AUTH_BLOCK_SELF, 26 STR_L2AUTH_BLOCK_BOT, 27 STR_L2AUTH_BLOCK_ABUSE, ...), or 0
	 * @param flags
	 *          bit mask; the lowest set bit selects a STR_PLAYNC_BLOCK_* message (bit 0 STR_PLAYNC_BLOCK_SELF, bit 1 STR_PLAYNC_BLOCK_BOT_AUTO, ...).
	 *          Used if the reason is 0.
	 */
	public SM_ACCOUNT_BANNED(int reason, int flags) {
		super(0x02);
		this.reason = reason;
		this.flags = flags;
	}

	@Override
	protected void writeImpl(LoginConnection con) {
		writeD(reason);
		writeD(flags);
	}
}
