package com.aionemu.loginserver.network;

import com.aionemu.commons.network.NioServer;
import com.aionemu.commons.network.ServerCfg;
import com.aionemu.loginserver.configs.Config;
import com.aionemu.loginserver.network.aion.AionConnectionFactoryImpl;
import com.aionemu.loginserver.network.gameserver.GsConnectionFactoryImpl;

/**
 * @author KID
 * @modified Neon
 */
public class NetConnector {

	/**
	 * NioServer instance that will handle io.
	 */
	private final static NioServer instance;

	static {
		ServerCfg aion = new ServerCfg(Config.CLIENT_SOCKET_ADDRESS, "Aion Connections", new AionConnectionFactoryImpl());
		ServerCfg gs = new ServerCfg(Config.GAMESERVER_SOCKET_ADDRESS, "GS Connections", new GsConnectionFactoryImpl());
		instance = new NioServer(Config.NIO_READ_WRITE_THREADS, gs, aion);
	}

	/**
	 * @return NioServer instance.
	 */
	public static NioServer getInstance() {
		return instance;
	}
}
