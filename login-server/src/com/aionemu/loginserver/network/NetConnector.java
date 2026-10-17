package com.aionemu.loginserver.network;

import java.util.concurrent.TimeUnit;

import com.aionemu.commons.network.netty.NettyPipeline;
import com.aionemu.commons.network.netty.NettyServer;
import com.aionemu.loginserver.configs.Config;
import com.aionemu.loginserver.network.aion.LoginConnection;
import com.aionemu.loginserver.network.gameserver.GsConnection;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * @author KID, Neon
 */
public class NetConnector {

	private static NettyServer nettyServer;
	private static EventExecutorGroup clientPacketExecutors;

	public static void connect() {
		nettyServer = new NettyServer(Config.TRANSPORT, Config.NIO_READ_WRITE_THREADS);
		clientPacketExecutors = new DefaultEventExecutorGroup(8, new DefaultThreadFactory("PacketProcessor"));
		nettyServer.bind(Config.CLIENT_SOCKET_ADDRESS, "Aion game clients", ch -> LoginConnection.initChannel(ch, clientPacketExecutors.next()));
		nettyServer.bind(Config.GAMESERVER_SOCKET_ADDRESS, "game servers", ch -> NettyPipeline.init(ch, new GsConnection(ch), 0xFFFF));
	}

	public static void shutdown() {
		nettyServer.shutdown();
		clientPacketExecutors.shutdownGracefully(100, 3000, TimeUnit.MILLISECONDS).awaitUninterruptibly();
		GsConnection.shutdownExecutors();
	}
}
