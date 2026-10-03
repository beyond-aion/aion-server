package com.aionemu.chatserver.network;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.network.NetworkConfig;
import com.aionemu.chatserver.network.aion.AionConnection;
import com.aionemu.chatserver.network.gameserver.GsConnection;
import com.aionemu.chatserver.network.netty.NettyConnection;
import com.aionemu.chatserver.network.netty.NettyPipeline;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class NetConnector {

	private static final Logger log = LoggerFactory.getLogger(NetConnector.class);
	private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	private static EventLoopGroup acceptorGroup;
	private static EventLoopGroup ioGroup;
	private static EventExecutorGroup packetExecutors;
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
		ioGroup = new MultiThreadIoEventLoopGroup(Math.max(1, NetworkConfig.NIO_READ_WRITE_THREADS), new DefaultThreadFactory("ReadWrite"),
			ioHandlerFactory);
		packetExecutors = new DefaultEventExecutorGroup(8, new DefaultThreadFactory("PacketProcessor"));
		bind(NetworkConfig.CLIENT_SOCKET_ADDRESS, "Aion game clients", 8192 * 2, NetworkConfig.MAX_CONNECTIONS_PER_IP, AionConnection::new);
		bind(NetworkConfig.GAMESERVER_SOCKET_ADDRESS, "game servers", 8192 * 8, 0, GsConnection::new);
	}

	private static void bind(InetSocketAddress address, String clientDescription, int maxPacketSize, int maxConnectionsPerIp,
		BiFunction<Channel, EventExecutor, NettyConnection<?>> connectionFactory) {
		Map<String, Integer> connectionsByIp = new ConcurrentHashMap<>();
		Channel serverChannel = new ServerBootstrap().group(acceptorGroup, ioGroup).channel(serverChannelClass)
			.childOption(ChannelOption.TCP_NODELAY, true)
			.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 256 * 1024))
			.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) {
					String ip = ch.remoteAddress().getAddress().getHostAddress();
					if (maxConnectionsPerIp > 0) {
						if (!tryAcquireConnectionSlot(connectionsByIp, ip, maxConnectionsPerIp)) {
							log.debug("Rejected connection from {}: limit of {} connections per IP reached", ip, maxConnectionsPerIp);
							ch.close();
							return;
						}
						ch.closeFuture().addListener(_ -> connectionsByIp.computeIfPresent(ip, (_, count) -> count > 1 ? count - 1 : null));
					}
					channels.add(ch);
					NettyPipeline.init(ch, connectionFactory.apply(ch, packetExecutors.next()), maxPacketSize);
				}
			}).bind(address).syncUninterruptibly().channel();
		channels.add(serverChannel);
		log.info("Listening on {}:{} for {}", address.getAddress().getHostAddress(), address.getPort(), clientDescription);
	}

	private static boolean tryAcquireConnectionSlot(Map<String, Integer> connectionsByIp, String ip, int maxConnectionsPerIp) {
		boolean[] acquired = { false };
		connectionsByIp.compute(ip, (_, count) -> {
			int current = count == null ? 0 : count;
			if (current >= maxConnectionsPerIp)
				return count;
			acquired[0] = true;
			return current + 1;
		});
		return acquired[0];
	}

	public static void shutdown() {
		channels.close().awaitUninterruptibly();
		acceptorGroup.shutdownGracefully().syncUninterruptibly();
		ioGroup.shutdownGracefully().syncUninterruptibly();
		packetExecutors.shutdownGracefully().syncUninterruptibly();
	}
}
