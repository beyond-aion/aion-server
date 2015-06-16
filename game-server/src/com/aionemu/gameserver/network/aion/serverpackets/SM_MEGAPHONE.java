package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Artur
 */
public class SM_MEGAPHONE extends AionServerPacket {

	private String senderName;
	private String message;
	private int itemId;

	public SM_MEGAPHONE(String senderName, String message, int itemId) {
		this.senderName = senderName;
		this.message = message; 
		this.itemId = itemId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeS(senderName);
		writeS(message);
		writeD(itemId);
	}
}
