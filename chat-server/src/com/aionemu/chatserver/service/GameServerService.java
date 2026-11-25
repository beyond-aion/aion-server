package com.aionemu.chatserver.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.network.NetworkConfig;
import com.aionemu.chatserver.network.gameserver.GsAuthResponse;
import com.aionemu.chatserver.network.gameserver.GsConnection;

/**
 * @author ATracer, KID, Neon
 */
public class GameServerService {

	private static final Logger log = LoggerFactory.getLogger(GameServerService.class);
	private static final GameServerService instance = new GameServerService();
	public static volatile byte GAMESERVER_ID;

	// The registered connection, not a flag: each connection runs on its own thread, and one that closes late must not unregister its successor
	private GsConnection registeredConnection;

	public static GameServerService getInstance() {
		return instance;
	}

	public synchronized GsAuthResponse registerGameServer(GsConnection connection, byte gameServerId, String password, boolean fromLoopback) {
		if (registeredConnection != null)
			return GsAuthResponse.ALREADY_REGISTERED;
		if (NetworkConfig.GAMESERVER_PASSWORD.isEmpty() && !fromLoopback) {
			log.warn("Rejected game server #{} from a remote address: chatserver.network.gameserver.password is not set", gameServerId);
			return GsAuthResponse.NOT_AUTHED;
		}
		if (!MessageDigest.isEqual(password.getBytes(StandardCharsets.UTF_8), NetworkConfig.GAMESERVER_PASSWORD.getBytes(StandardCharsets.UTF_8)))
			return GsAuthResponse.NOT_AUTHED;
		registeredConnection = connection;
		GAMESERVER_ID = gameServerId;
		return GsAuthResponse.AUTHED;
	}

	public synchronized void setOffline(GsConnection connection) {
		if (registeredConnection != connection)
			return;
		log.info("Game server #{} disconnected", GAMESERVER_ID);
		registeredConnection = null;
	}
}
