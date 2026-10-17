package com.aionemu.commons.network.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.NetworkUtils;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Network threads of a server, with its listening sockets and its connections to other servers.
 */
public class NettyServer {

	private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

	private final Transport.Implementation transport;
	private final EventLoopGroup acceptorGroup;
	private final EventLoopGroup ioGroup;
	private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	/**
	 * @param ioThreads
	 *          Number of threads that read and write the connections' data. If it's less than 1, the thread that accepts connections does it.
	 * @throws IllegalStateException
	 *           if the transport is not available on this system
	 */
	public NettyServer(Transport transport, int ioThreads) {
		this.transport = transport.select();
		IoHandlerFactory ioHandlerFactory = this.transport.newIoHandlerFactory();
		if (ioThreads < 1) {
			acceptorGroup = ioGroup = new MultiThreadIoEventLoopGroup(1, new DefaultThreadFactory("AcceptReadWrite"), ioHandlerFactory);
		} else {
			acceptorGroup = new MultiThreadIoEventLoopGroup(1, new DefaultThreadFactory("Acceptor"), ioHandlerFactory);
			ioGroup = new MultiThreadIoEventLoopGroup(ioThreads, new DefaultThreadFactory("ReadWrite"), ioHandlerFactory);
		}
	}

	/**
	 * Listens on the given address. The initializer sets up the pipeline of each accepted channel with {@link NettyPipeline}, or closes it.
	 */
	public void bind(InetSocketAddress address, String clientDescription, Consumer<Channel> channelInitializer) {
		Channel serverChannel = new ServerBootstrap().group(acceptorGroup, ioGroup).channel(transport.serverChannelClass)
			.childOption(ChannelOption.TCP_NODELAY, true)
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			.childHandler(initializer(channelInitializer))
			.bind(address).syncUninterruptibly().channel();
		channels.add(serverChannel);
		log.info("Listening on {} for {}", NetworkUtils.getAddressInfo(address), clientDescription);
	}

	/**
	 * Connects to the given address. The initializer sets up the pipeline of the channel with {@link NettyPipeline}.
	 *
	 * @return The future of the connection attempt.
	 */
	public ChannelFuture connect(InetSocketAddress address, Consumer<Channel> channelInitializer) {
		return new Bootstrap().group(ioGroup).channel(transport.channelClass)
			.option(ChannelOption.TCP_NODELAY, true)
			.option(ChannelOption.SO_KEEPALIVE, true)
			.handler(initializer(channelInitializer))
			.connect(address);
	}

	private ChannelInitializer<Channel> initializer(Consumer<Channel> channelInitializer) {
		return new ChannelInitializer<>() {

			@Override
			protected void initChannel(Channel ch) {
				channels.add(ch);
				channelInitializer.accept(ch);
			}
		};
	}

	/**
	 * @return The connections of the given type that are currently open.
	 */
	public <T extends NettyConnection<?>> Stream<T> findConnections(Class<T> type) {
		return channels.stream().map(NettyPipeline::getConnection).filter(type::isInstance).map(type::cast);
	}

	/**
	 * Stops accepting connections, closes all connections after sending their pending data (or after a timeout), and stops the network threads.
	 * Afterwards, every connection has queued its {@link NettyConnection#onDisconnect()} on its packet executor.
	 */
	public void shutdown() {
		channels.close(ServerChannel.class::isInstance).awaitUninterruptibly();
		findConnections(NettyConnection.class).forEach(NettyConnection::close);
		long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
		for (Channel channel : channels)
			channel.closeFuture().awaitUninterruptibly(Math.max(0, TimeUnit.NANOSECONDS.toMillis(deadlineNanos - System.nanoTime())));
		channels.close().awaitUninterruptibly();
		long quietPeriodMillis = 100;
		long timeoutMillis = 3000;
		Future<?> ioShutdown = ioGroup.shutdownGracefully(quietPeriodMillis, timeoutMillis, TimeUnit.MILLISECONDS);
		Future<?> acceptorShutdown = acceptorGroup.shutdownGracefully(quietPeriodMillis, timeoutMillis, TimeUnit.MILLISECONDS);
		ioShutdown.awaitUninterruptibly();
		acceptorShutdown.awaitUninterruptibly();
	}
}
