package com.aionemu.gameserver.network.chatserver.serverpackets;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.network.chatserver.ChatServerConnection;
import com.aionemu.gameserver.network.chatserver.CsServerPacket;

/**
 * @author ATracer
 */
public class SM_CS_PLAYER_AUTH extends CsServerPacket {

	private int playerId;
	private String playerLogin;
	private String nick;
	private int accessLevel;
	private Race race;

	public SM_CS_PLAYER_AUTH(int playerId, String playerLogin, String nick, int accessLevel, Race race) {
		super(0x01);
		this.playerId = playerId;
		this.playerLogin = playerLogin;
		this.nick = nick;
		this.accessLevel = accessLevel;
		this.race = race;
	}

	@Override
	protected void writeImpl(ChatServerConnection con) {
		writeD(playerId);
		writeS(playerLogin);
		writeS(nick);
		writeD(accessLevel);
		writeD(race.getRaceId());
	}
}
