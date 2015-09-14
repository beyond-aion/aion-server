package com.aionemu.loginserver.network.gameserver.serverpackets;

import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.GsServerPacket;

/**
 * @author cura
 */
public class SM_GS_CHARACTER_RESPONSE extends GsServerPacket {

	private final int accountId;

	/**
	 * @param accountId
	 */
	public SM_GS_CHARACTER_RESPONSE(int accountId) {
		this.accountId = accountId;
	}

	@Override
	protected void writeImpl(GsConnection con) {
		writeC(8);
		writeD(accountId);
	}
}
