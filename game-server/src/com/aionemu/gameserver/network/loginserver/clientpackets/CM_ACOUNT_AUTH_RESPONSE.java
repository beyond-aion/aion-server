package com.aionemu.gameserver.network.loginserver.clientpackets;

import com.aionemu.gameserver.model.account.AccountTime;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.network.loginserver.LsClientPacket;

/**
 * In this packet LoginServer is answering on GameServer request about valid authentication data and also sends account name of user that is
 * authenticating on GameServer.
 * 
 * @author -Nemesiss-
 */
public class CM_ACOUNT_AUTH_RESPONSE extends LsClientPacket {

	public CM_ACOUNT_AUTH_RESPONSE(int opCode) {
		super(opCode);
	}

	/**
	 * accountId
	 */
	private int accountId;

	/**
	 * result - true = authed
	 */
	private boolean result;

	/**
	 * accountName [if response is ok]
	 */
	private String accountName;
	/**
	 * Time of account creation, measured in milliseconds since 1.1.1970 0:00 UTC
	 */
	private long creationDate;
	/**
	 * accountTime
	 */
	private AccountTime accountTime;
	/**
	 * access level - regular/gm/admin
	 */
	private byte accessLevel;
	/**
	 * Membership - regular/premium
	 */
	private byte membership;

	/**
	 * Toll
	 */
	private long toll;
	/**
	 * Allowed HDD serial
	 */
	private String allowedHddSerial;

	@Override
	public void readImpl() {
		accountId = readD();
		result = readC() == 1;

		if (result) {
			accountName = readS();
			creationDate = readQ();
			accountTime = new AccountTime();

			accountTime.setAccumulatedOnlineTime(readQ());
			accountTime.setAccumulatedRestTime(readQ());

			accessLevel = readC();
			membership = readC();
			toll = readQ();
			allowedHddSerial = readS();
		}
	}

	@Override
	public void runImpl() {
		LoginServer.getInstance().accountAuthenticationResponse(accountId, accountName, result, creationDate, accountTime, accessLevel, membership, toll,
			allowedHddSerial);
	}
}
