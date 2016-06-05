package com.aionemu.loginserver.network.gameserver.serverpackets;

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

	/**
	 * Account id
	 */
	private final int accountId;

	/**
	 * True if account is authenticated.
	 */
	private final boolean ok;

	/**
	 * account name
	 */
	private final String accountName;

	/**
	 * Time of account creation, measured in milliseconds since 1.1.1970 UTC
	 */
	private final long creationDate;

	private final byte accessLevel, membership;
	private final long toll;
	private final String allowedHddSerial;

	public SM_ACCOUNT_AUTH_RESPONSE(int accountId, boolean ok, String accountName, long creationDate, byte accessLevel, byte membership, long toll,
		String allowedHddSerial) {
		this.accountId = accountId;
		this.ok = ok;
		this.accountName = accountName;
		this.creationDate = creationDate;
		this.accessLevel = accessLevel;
		this.membership = membership;
		this.toll = toll;
		this.allowedHddSerial = allowedHddSerial;
	}

	@Override
	protected void writeImpl(GsConnection con) {
		writeC(1);
		writeD(accountId);
		writeC(ok ? 1 : 0);

		if (ok) {
			writeS(accountName);
			writeQ(creationDate);

			AccountTime accountTime = con.getGameServerInfo().getAccountFromGameServer(accountId).getAccountTime();

			writeQ(accountTime.getAccumulatedOnlineTime());
			writeQ(accountTime.getAccumulatedRestTime());
			writeC(accessLevel);
			writeC(membership);
			writeQ(toll);
			writeS(allowedHddSerial);
		}
	}
}
