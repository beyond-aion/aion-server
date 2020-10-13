package com.aionemu.gameserver.network.chatserver.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
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
	private byte accessLevel;

	public SM_CS_PLAYER_AUTH(Player player) {
		super(0x01);
		this.playerId = player.getObjectId();
		this.accName = player.getAccountName();
		this.nick = player.getName(true);
		this.raceId = player.getRace().getRaceId();
		this.accessLevel = player.getAccount().getAccessLevel();
	}

	@Override
	protected void writeImpl(ChatServerConnection con) {
		writeD(playerId);
		writeS(accName);
		writeS(nick);
		writeD(raceId);
		writeC(accessLevel);
	}
}
