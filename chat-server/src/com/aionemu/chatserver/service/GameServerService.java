package com.aionemu.chatserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.network.NetworkConfig;
import com.aionemu.chatserver.network.gameserver.GsAuthResponse;

/**
 * @author ATracer, KID, Neon
 */
public class GameServerService {

	private static final Logger log = LoggerFactory.getLogger(GameServerService.class);
	private static final GameServerService instance = new GameServerService();
	public static byte GAMESERVER_ID;

	private boolean isOnline = false;

	public static GameServerService getInstance() {
		return instance;
	}

	public GsAuthResponse registerGameServer(byte gameServerId, String password) {
		if (isOnline)
			return GsAuthResponse.ALREADY_REGISTERED;
		if (!password.equals(NetworkConfig.GAMESERVER_PASSWORD))
			return GsAuthResponse.NOT_AUTHED;
		isOnline = true;
		GAMESERVER_ID = gameServerId;
		return GsAuthResponse.AUTHED;
	}

	public void setOffline() {
		log.info("Gameserver #{} is disconnected", GAMESERVER_ID);
		isOnline = false;
	}
}
