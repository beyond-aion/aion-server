package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author xavier
 */
public class SM_UPDATE_NOTE extends AionServerPacket {

	private final int targetObjId;
	private final String note;

	public SM_UPDATE_NOTE(Player player) {
		this.targetObjId = player.getObjectId();
		this.note = player.getCommonData().getNote();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(targetObjId);
		writeS(note);
	}
}
