package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author pixfid, Rolandas, Yeats, Neon
 */
public class SM_ACCOUNT_PROPERTIES extends AionServerPacket {

	public SM_ACCOUNT_PROPERTIES() {
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(con.getAccount().getAccessLevel() >= AdminConfig.GM_PANEL ? 1 : 0); // enables GM panel and other windows, also disables client-side faction restriction for char creation
		writeD(0); // always 0
		writeH(0); // 0 or 52168 (C8 CB)
		writeC(0); // 0 or 1
		writeH(0); // 0, 4 or 5
		writeC(0); // 0, 4 or 124
		writeH(0); // can be 1
		writeD(0); // 0 or 16 (or 31 = strong energy of repose)
		writeD(0); // always 0
		writeD(0); // purchased packet (8 = gold pack)
		writeD(4); // account status (0 = gold-user, 1/2 = starter, 3/4 = veteran)
	}
}
