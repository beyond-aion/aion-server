package com.aionemu.loginserver.network.gameserver.serverpackets;

import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.model.AccountTime;
import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.GsServerPacket;

/**
 * In this packet LoginServer is answering on GameServer request about valid authentication data and also sends account name of user that is
 * authenticating on GameServer.
 * 
 * @author -Nemesiss-
 */
public class SM_ACCOUNT_AUTH_RESPONSE extends GsServerPacket {

	private final int accountId;
	private final boolean ok;
	private final String accountName;
	private final long creationDate;
	private final long accumulatedOnlineTime, accumulatedRestTime;
	private final byte accessLevel, membership;
	private final String allowedHddSerial;

	/**
	 * @param account
	 *          The authenticated account, or null if the authentication failed. Its data is copied here, since the packet is written later on a
	 *          network thread, when the account may already have left the game server again.
	 */
	public SM_ACCOUNT_AUTH_RESPONSE(int accountId, Account account) {
		this.accountId = accountId;
		this.ok = account != null;
		this.accountName = ok ? account.getName() : null;
		this.creationDate = ok ? account.getCreationDate().getTime() : 0;
		AccountTime accountTime = ok ? account.getAccountTime() : null;
		this.accumulatedOnlineTime = ok ? accountTime.getAccumulatedOnlineTime() : 0;
		this.accumulatedRestTime = ok ? accountTime.getAccumulatedRestTime() : 0;
		this.accessLevel = ok ? account.getAccessLevel() : 0;
		this.membership = ok ? account.getMembership() : 0;
		this.allowedHddSerial = ok ? account.getAllowedHddSerial() : null;
	}

	@Override
	protected void writeImpl(GsConnection con) {
		writeC(1);
		writeD(accountId);
		writeC(ok ? 1 : 0);
		if (ok) {
			writeS(accountName);
			writeQ(creationDate);
			writeQ(accumulatedOnlineTime);
			writeQ(accumulatedRestTime);
			writeC(accessLevel);
			writeC(membership);
			writeS(allowedHddSerial);
		}
	}
}
