package com.aionemu.gameserver.network.chatserver.serverpackets;

import com.aionemu.gameserver.network.chatserver.ChatServerConnection;
import com.aionemu.gameserver.network.chatserver.CsServerPacket;

/**
 * @author ViAl
 */
public class SM_CS_PLAYER_GAG extends CsServerPacket {

	private int playerId;
	private long gagTime;

	public SM_CS_PLAYER_GAG(int playerId, long gagTime) {
		super(0x03);
		this.playerId = playerId;
		this.gagTime = gagTime;
	}

	@Override
	protected void writeImpl(ChatServerConnection con) {
		writeD(playerId);
		writeQ(gagTime);
	}
}
