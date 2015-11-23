package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author pixfid, Rolandas
 * @modified Yeats & Neon
 */
public class SM_ACCOUNT_PROPERTIES extends AionServerPacket {

	public SM_ACCOUNT_PROPERTIES() {
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(con.getAccount().getAccessLevel() >= AdminConfig.GM_PANEL ? 1 : 0); // enables GM panel and other windows, also disables client-side faction restriction for char creation
		writeH(0);
		writeD(0);
		writeD(0); // gold pack related (0 = no chat restriction)
		writeD(0);
		writeD(0);
		writeC(0);
		writeD(0); // gold pack time limit (8 = unlimited?), 0 when veteran
		writeD(4); // account status (0 = gold-user, 1/2 = starter, 3/4 = veteran)
	}
}
