package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Simple
 */
public class SM_PRIVATE_STORE_NAME extends AionServerPacket {

	/** Private store Information **/
	private int playerObjId;
	private String name;

	public SM_PRIVATE_STORE_NAME(Player player) {
		this.playerObjId = player.getObjectId();
		this.name = player.getStore().getStoreMessage();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(playerObjId);
		writeS(name);
	}
}
