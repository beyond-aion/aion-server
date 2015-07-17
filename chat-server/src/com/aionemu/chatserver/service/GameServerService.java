package com.aionemu.chatserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.Config;
import com.aionemu.chatserver.network.gameserver.GsAuthResponse;

/**
 * @author ATracer, KID
 */
public class GameServerService
{
	private Logger log = LoggerFactory.getLogger(GameServerService.class);
	private static GameServerService instance = new GameServerService();
	public static byte GAMESERVER_ID;
	private boolean isOnline = false;
	
	public static GameServerService getInstance()
	{
		return instance;
	}
	
	/**
	 * @param gameChannelHandler
	 * @param gameServerId
	 * @param defaultAddress
	 * @param password
	 * @return
	 */
	public GsAuthResponse registerGameServer(byte gameServerId, byte[] defaultAddress, String password)
	{
		GAMESERVER_ID = gameServerId;
		if(isOnline)
			return GsAuthResponse.ALREADY_REGISTERED;
		return passwordConfigAuth(password);
	}
	
	/**
	 * @return
	 */
	private GsAuthResponse passwordConfigAuth(String password)
	{
		if (password.equals(Config.GAME_SERVER_PASSWORD))
		{
			isOnline = true;
			return GsAuthResponse.AUTHED;
		}
		log.warn("Gameserver #"+GAMESERVER_ID+" has invalid password.");
		return GsAuthResponse.NOT_AUTHED;
	}
	
	public void setOffline()
	{
		log.info("Gameserver #"+GAMESERVER_ID+" is disconnected");
		isOnline = false;
	}
}
