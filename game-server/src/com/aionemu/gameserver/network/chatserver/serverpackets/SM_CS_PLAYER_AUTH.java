package com.aionemu.gameserver.network.chatserver.serverpackets;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.network.chatserver.ChatServerConnection;
import com.aionemu.gameserver.network.chatserver.CsServerPacket;

/**
 * @author ATracer
 */
public class SM_CS_PLAYER_AUTH extends CsServerPacket {

	private int playerId;
	private String accName;
	private String nick;
	private int raceId;

	public SM_CS_PLAYER_AUTH(int playerId, String accName, String nick, Race race) {
		super(0x01);
		this.playerId = playerId;
		this.accName = accName;
		this.nick = nick;
		this.raceId = race.getRaceId();
	}

	@Override
	protected void writeImpl(ChatServerConnection con) {
		writeD(playerId);
		writeS(accName);
		writeS(nick);
		writeD(raceId);
	}
}
