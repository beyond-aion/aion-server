package com.aionemu.loginserver.network;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.netty.NettyConnection;
import com.aionemu.commons.network.netty.NettyPipeline;
import com.aionemu.commons.utils.NetworkUtils;
import com.aionemu.loginserver.configs.Config;
import com.aionemu.loginserver.network.aion.LoginConnection;
import com.aionemu.loginserver.network.gameserver.GsConnection;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.*;

/**
 * @author KID, Neon
 */
public class NetConnector {

	private static final Logger log = LoggerFactory.getLogger(NetConnector.class);
	private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	private static EventLoopGroup acceptorGroup;
	private static EventLoopGroup ioGroup;
	private static EventExecutorGroup clientPacketExecutors;
	private static Class<? extends ServerChannel> serverChannelClass;

	public static void connect() {
		IoHandlerFactory ioHandlerFactory;
		if (Epoll.isAvailable()) {
			ioHandlerFactory = EpollIoHandler.newFactory();
			serverChannelClass = EpollServerSocketChannel.class;
			log.info("Using the epoll transport");
		} else {
			ioHandlerFactory = NioIoHandler.newFactory();
			serverChannelClass = NioServerSocketChannel.class;
			log.debug("Using the NIO transport, epoll is unavailable: {}", Epoll.unavailabilityCause().toString());
		}
		acceptorGroup = new MultiThreadIoEventLoopGroup(1, new DefaultThreadFactory("Acceptor"), ioHandlerFactory);
		ioGroup = new MultiThreadIoEventLoopGroup(Math.max(1, Config.NIO_READ_WRITE_THREADS), new DefaultThreadFactory("ReadWrite"),
			ioHandlerFactory);
		clientPacketExecutors = new DefaultEventExecutorGroup(8, new DefaultThreadFactory("PacketProcessor"));
		bind(Config.CLIENT_SOCKET_ADDRESS, "Aion game clients", 8192 * 2, ch -> new LoginConnection(ch, clientPacketExecutors.next()));
		bind(Config.GAMESERVER_SOCKET_ADDRESS, "game servers", 8192 * 8, GsConnection::new);
	}

	private static void bind(InetSocketAddress address, String clientDescription, int maxPacketSize,
		Function<Channel, NettyConnection<?>> connectionFactory) {
		Channel serverChannel = new ServerBootstrap().group(acceptorGroup, ioGroup).channel(serverChannelClass)
			.childOption(ChannelOption.TCP_NODELAY, true)
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) {
					channels.add(ch);
					NettyPipeline.init(ch, connectionFactory.apply(ch), maxPacketSize);
				}
			}).bind(address).syncUninterruptibly().channel();
		channels.add(serverChannel);
		log.info("Listening on {} for {}", NetworkUtils.getAddressInfo(address), clientDescription);
	}

	public static void shutdown() {
		channels.close().awaitUninterruptibly();
		long quietPeriodMillis = 100;
		long timeoutMillis = 3000;
		Future<?> acceptorShutdown = acceptorGroup.shutdownGracefully(quietPeriodMillis, timeoutMillis, TimeUnit.MILLISECONDS);
		Future<?> ioShutdown = ioGroup.shutdownGracefully(quietPeriodMillis, timeoutMillis, TimeUnit.MILLISECONDS);
		Future<?> packetShutdown = clientPacketExecutors.shutdownGracefully(quietPeriodMillis, timeoutMillis, TimeUnit.MILLISECONDS);
		acceptorShutdown.awaitUninterruptibly();
		ioShutdown.awaitUninterruptibly();
		packetShutdown.awaitUninterruptibly();
		GsConnection.shutdownExecutors();
	}
}
