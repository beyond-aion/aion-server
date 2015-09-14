package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Sends Survey HTML data to the client. This packet can be splitted over max 255 packets The max length of the HTML may therefore be 255 * 65525 byte
 * 
 * @author lhw and Kaipo
 */
public class SM_QUESTIONNAIRE extends AionServerPacket {

	private int messageId;
	private byte chunk;
	private byte count;
	private String html;

	public SM_QUESTIONNAIRE(int messageId, byte chunk, byte count, String html) {
		this.messageId = messageId;
		this.chunk = chunk;
		this.count = count;
		this.html = html;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(messageId);
		writeC(chunk);
		writeC(count);
		writeH(html.length() * 2);
		writeS(html);
	}
}
