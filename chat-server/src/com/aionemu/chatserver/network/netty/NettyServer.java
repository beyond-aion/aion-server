package com.aionemu.chatserver.network.netty;

import java.nio.ByteOrder;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.network.NetworkConfig;
import com.aionemu.chatserver.network.aion.ClientPacketHandler;
import com.aionemu.chatserver.network.gameserver.GsConnectionFactoryImpl;
import com.aionemu.chatserver.network.netty.pipeline.LoginToClientPipeLineFactory;
import com.aionemu.commons.network.NioServer;
import com.aionemu.commons.network.ServerCfg;

/**
 * @author ATracer
 * @modified Neon
 */
public class NettyServer {

	private static NettyServer instance = new NettyServer();
	private ChannelFactory aionClientChannelFactory;
	private ChannelGroup aionClientChannelGroup;
	private NioServer nioServer;

	public static NettyServer getInstance() {
		return instance;
	}

	private NettyServer() {
		aionClientChannelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(),
			NetworkConfig.NIO_READ_WRITE_THREADS + 1);
		aionClientChannelGroup = new DefaultChannelGroup(NettyServer.class.getName());
		aionClientChannelGroup.add(initChannel()); // why not handle aion client packets via NioServer?
		LoggerFactory.getLogger(this.getClass()).info("Server listening on "
			+ (NetworkConfig.CLIENT_SOCKET_ADDRESS.getAddress().isAnyLocalAddress() ? "all interfaces,"
				: "IP: " + NetworkConfig.CLIENT_SOCKET_ADDRESS.getAddress().getHostAddress())
			+ " Port: " + NetworkConfig.CLIENT_SOCKET_ADDRESS.getPort() + " for Aion Connections");

		nioServer = new NioServer(NetworkConfig.NIO_READ_WRITE_THREADS,
			new ServerCfg(NetworkConfig.GAMESERVER_SOCKET_ADDRESS, "GS Connections", new GsConnectionFactoryImpl()));
		nioServer.connect(Executors.newSingleThreadExecutor());
	}

	/**
	 * @param channelFactory
	 * @param listenAddress
	 * @param port
	 * @param channelPipelineFactory
	 * @return Channel
	 */
	private Channel initChannel() {
		ServerBootstrap bootstrap = new ServerBootstrap(aionClientChannelFactory);
		bootstrap.setPipelineFactory(new LoginToClientPipeLineFactory(new ClientPacketHandler()));
		bootstrap.setOption("child.bufferFactory", HeapChannelBufferFactory.getInstance(ByteOrder.LITTLE_ENDIAN));
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		bootstrap.setOption("child.reuseAddress", true);
		bootstrap.setOption("child.connectTimeoutMillis", 100);
		bootstrap.setOption("readWriteFair", true);
		return bootstrap.bind(NetworkConfig.CLIENT_SOCKET_ADDRESS);
	}

	/**
	 * Shutdown server
	 */
	public void shutdownAll() {
		aionClientChannelGroup.close().awaitUninterruptibly();
		aionClientChannelFactory.releaseExternalResources();
		nioServer.shutdown();
	}
}
