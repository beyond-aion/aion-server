package com.aionemu.gameserver.network.loginserver.serverpackets;

import com.aionemu.gameserver.network.loginserver.LoginServerConnection;
import com.aionemu.gameserver.network.loginserver.LsServerPacket;

/**
 * @author cura
 */
public class SM_GS_CHARACTER extends LsServerPacket {

	private int accountId;
	private int characterCount;

	/**
	 * @param accountId
	 * @param characterCount
	 */
	public SM_GS_CHARACTER(final int accountId, final int characterCount) {
		super(0x08);
		this.accountId = accountId;
		this.characterCount = characterCount;
	}

	@Override
	protected void writeImpl(LoginServerConnection con) {
		writeD(accountId);
		writeC(characterCount);
	}
}
