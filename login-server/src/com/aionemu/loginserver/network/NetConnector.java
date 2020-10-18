package com.aionemu.loginserver.network;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.aionemu.commons.network.NioServer;
import com.aionemu.commons.network.ServerCfg;
import com.aionemu.loginserver.configs.Config;
import com.aionemu.loginserver.network.aion.AionConnectionFactoryImpl;
import com.aionemu.loginserver.network.gameserver.GsConnectionFactoryImpl;

/**
 * @author KID, Neon
 */
public class NetConnector {

	/**
	 * NioServer instance that will handle io.
	 */
	private final static NioServer instance;
	private final static ExecutorService dcExecutor = Executors.newCachedThreadPool();

	static {
		ServerCfg aion = new ServerCfg(Config.CLIENT_SOCKET_ADDRESS, "Aion Connections", new AionConnectionFactoryImpl());
		ServerCfg gs = new ServerCfg(Config.GAMESERVER_SOCKET_ADDRESS, "GS Connections", new GsConnectionFactoryImpl());
		instance = new NioServer(Config.NIO_READ_WRITE_THREADS, gs, aion);
	}

	public static void connect() {
		instance.connect(dcExecutor);
	}

	public static void shutdown() {
		instance.shutdown();
		dcExecutor.shutdown();
	}
}
