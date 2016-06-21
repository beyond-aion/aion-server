package com.aionemu.gameserver.network.loginserver.serverpackets;

import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.network.loginserver.LoginServerConnection;
import com.aionemu.gameserver.network.loginserver.LsServerPacket;

/**
 * @author ViAl
 */
public class SM_CHANGE_ALLOWED_HDD_SERIAL extends LsServerPacket {

	private int accountId;
	private String hddSerial;

	public SM_CHANGE_ALLOWED_HDD_SERIAL(Account playerAccount) {
		super(15);
		this.accountId = playerAccount.getId();
		this.hddSerial = playerAccount.getAllowedHddSerial();
	}

	@Override
	protected void writeImpl(LoginServerConnection con) {
		writeD(accountId);
		writeS(hddSerial);
	}
}
