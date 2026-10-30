package com.aionemu.chatserver.network;

import java.util.concurrent.TimeUnit;

import com.aionemu.chatserver.configs.network.NetworkConfig;
import com.aionemu.chatserver.network.aion.AionConnection;
import com.aionemu.chatserver.network.gameserver.GsConnection;
import com.aionemu.commons.network.netty.NettyPipeline;
import com.aionemu.commons.network.netty.NettyServer;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;

public class NetConnector {

	private static NettyServer nettyServer;
	private static EventExecutorGroup packetExecutors;

	public static void connect() {
		nettyServer = new NettyServer(NetworkConfig.TRANSPORT, NetworkConfig.IO_THREADS);
		packetExecutors = new DefaultEventExecutorGroup(8, new DefaultThreadFactory("PacketProcessor"));
		nettyServer.bind(NetworkConfig.CLIENT_SOCKET_ADDRESS, "Aion game clients",
			ch -> NettyPipeline.init(ch, new AionConnection(ch, packetExecutors.next()), 8192 * 2));
		nettyServer.bind(NetworkConfig.GAMESERVER_SOCKET_ADDRESS, "game servers",
			ch -> NettyPipeline.init(ch, new GsConnection(ch, packetExecutors.next()), 0xFFFF));
	}

	public static void shutdown() {
		nettyServer.shutdown();
		packetExecutors.shutdownGracefully(100, 3000, TimeUnit.MILLISECONDS).awaitUninterruptibly();
	}
}
