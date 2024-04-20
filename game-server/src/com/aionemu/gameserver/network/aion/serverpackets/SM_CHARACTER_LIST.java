package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.network.aion.AionConnection;

/**
 * In this packet Server is sending Character List to client.
 * 
 * @author Nemesiss, AEJTester, Neon
 */
public class SM_CHARACTER_LIST extends AbstractPlayerInfoPacket {

	/**
	 * PlayOk2 - we dont care...
	 */
	private final int playOk2;

	/**
	 * Constructs new <tt>SM_CHARACTER_LIST </tt> packet
	 */
	public SM_CHARACTER_LIST(int playOk2) {
		this.playOk2 = playOk2;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Account account = con.getAccount();

		writeD(playOk2);
		writeC(account.size()); // character count
		for (PlayerAccountData playerData : account.getPlayerAccDataList()) {
			writePlayerInfo(playerData);
		}
	}
}
