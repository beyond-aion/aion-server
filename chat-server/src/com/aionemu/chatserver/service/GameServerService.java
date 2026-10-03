package com.aionemu.chatserver.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

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

	public GsAuthResponse registerGameServer(byte gameServerId, String password, boolean fromLoopback) {
		if (isOnline)
			return GsAuthResponse.ALREADY_REGISTERED;
		if (NetworkConfig.GAMESERVER_PASSWORD.isEmpty() && !fromLoopback) {
			log.warn("Rejected game server #{} from a remote address: chatserver.network.gameserver.password is not set", gameServerId);
			return GsAuthResponse.NOT_AUTHED;
		}
		if (!MessageDigest.isEqual(password.getBytes(StandardCharsets.UTF_8), NetworkConfig.GAMESERVER_PASSWORD.getBytes(StandardCharsets.UTF_8)))
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
