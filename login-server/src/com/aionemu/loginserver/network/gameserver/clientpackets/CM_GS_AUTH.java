package com.aionemu.loginserver.network.gameserver.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.GameServerTable;
import com.aionemu.loginserver.network.gameserver.GsAuthResponse;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;
import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.GsConnection.State;
import com.aionemu.loginserver.network.gameserver.serverpackets.SM_GS_AUTH_RESPONSE;

/**
 * This is authentication packet that gs will send to login server for registration.
 * 
 * @author -Nemesiss-
 */
public class CM_GS_AUTH extends GsClientPacket {

	private final Logger log = LoggerFactory.getLogger(CM_GS_AUTH.class);
	/**
	 * Password for authentication
	 */
	private String password;

	/**
	 * Id of GameServer
	 */
	private byte gameServerId;

	/**
	 * Maximum number of players that this Gameserver can accept.
	 */
	private int maxPlayers;

	private byte minAccessLevel;

	/**
	 * Port of this Gameserver.
	 */
	private int port;

	/**
	 * Default address for server
	 */
	private byte[] ip;

	@Override
	protected void readImpl() {
		gameServerId = readC();
		password = readS();
		byte length = readC();
		ip = readB(length);
		port = readUH();
		minAccessLevel = readC();
		maxPlayers = readD();
	}

	@Override
	protected void runImpl() {
		GsConnection client = getConnection();

		GsAuthResponse resp = GameServerTable.registerGameServer(client, gameServerId, password, ip, port, minAccessLevel, maxPlayers);
		switch (resp) {
			case AUTHED:
				log.info("Gameserver #" + gameServerId + " is now online");
				client.setState(State.AUTHED);
				client.sendPacket(new SM_GS_AUTH_RESPONSE(resp));
				break;
			default:
				client.close(new SM_GS_AUTH_RESPONSE(resp));
		}
	}
}
