package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Fast track info and response when trying to switch servers
 * 
 * @author xTz
 */
public class SM_UNK_3_5_1 extends AionServerPacket {

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(1);
		writeD(0);
		writeD(con.getActivePlayer().getObjectId());
		writeD(NetworkConfig.GAMESERVER_ID);
		writeD(0);
		writeD(0);
	}
}
