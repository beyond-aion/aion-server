package com.aionemu.chatserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.Config;
import com.aionemu.chatserver.network.gameserver.GsAuthResponse;

/**
 * @author ATracer, KID
 * @modified Neon
 */
public class GameServerService {

	private Logger log = LoggerFactory.getLogger(GameServerService.class);
	private static GameServerService instance = new GameServerService();
	public static byte GAMESERVER_ID;
	private boolean isOnline = false;

	public static GameServerService getInstance() {
		return instance;
	}

	/**
	 * @param gameChannelHandler
	 * @param gameServerId
	 * @param password
	 * @return
	 */
	public GsAuthResponse registerGameServer(byte gameServerId, String password) {
		if (isOnline)
			return GsAuthResponse.ALREADY_REGISTERED;
		if (!password.equals(Config.GAMESERVER_PASSWORD))
			return GsAuthResponse.NOT_AUTHED;
		isOnline = true;
		GAMESERVER_ID = gameServerId;
		return GsAuthResponse.AUTHED;
	}

	public void setOffline() {
		log.info("Gameserver #" + GAMESERVER_ID + " is disconnected");
		isOnline = false;
	}
}
