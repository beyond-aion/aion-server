package com.aionemu.gameserver.network.aion;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.aionemu.commons.network.netty.NettyServer;
import com.aionemu.commons.network.netty.Transport;
import com.aionemu.gameserver.configs.network.NetworkConfig;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.uring.IoUring;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.ReferenceCountUtil;

/**
 * Shutdown of the game client network under load: a third of the clients reads, a third doesn't read at all (full send buffers), a third resets
 * its connection while the server is writing to it. {@link NettyServer#shutdown()} has to return within a few seconds on every transport.
 */
public class AionShutdownStressTest {

	private static final int CLIENTS = Integer.getInteger("shutdown.clients", 300);
	private static final int PACKETS = Integer.getInteger("shutdown.packets", 300);

	@BeforeAll
	public static void initConfig() {
		NetworkConfig.PACKET_PROCESSOR_MAX_PENDING_PACKETS_PER_CONNECTION = 500;
		NetworkConfig.MAX_PENDING_SEND_KILOBYTES = Integer.getInteger("shutdown.max_pending_kb", 4096);
		NetworkConfig.MAX_SEND_STALL_SECONDS = 60;
		NetworkConfig.CLIENT_AUTH_TIMEOUT_SECONDS = 0;
	}

	@TestFactory
	public Stream<DynamicTest> shutdownReturnsUnderLoad() {
		return Stream.of(Transport.NIO, Transport.EPOLL, Transport.IO_URING).flatMap(t -> Stream.of(1, 4)
			.flatMap(n -> Stream.of(1, 2, 3).map(r -> DynamicTest.dynamicTest(t + ", " + n + " I/O thread(s), run " + r, () -> shutdownReturns(t, n)))));
	}

	private void shutdownReturns(Transport transport, int ioThreads) throws Exception {
		Assumptions.assumeTrue(transport != Transport.EPOLL || Epoll.isAvailable(), "epoll is unavailable");
		Assumptions.assumeTrue(transport != Transport.IO_URING || IoUring.isAvailable(), "io_uring is unavailable");
		int port;
		try (ServerSocket s = new ServerSocket(0)) {
			port = s.getLocalPort();
		}
		NettyServer server = new NettyServer(transport, ioThreads);
		List<AionConnection> connections = new ArrayList<>();
		server.bind(new InetSocketAddress("127.0.0.1", port), "shutdown test clients", channel -> {
			AionConnection connection = new AionConnection(channel);
			AionChannelInitializer.init(channel, connection);
			synchronized (connections) {
				connections.add(connection);
			}
		});
		EventLoopGroup clientGroup = new MultiThreadIoEventLoopGroup(4, NioIoHandler.newFactory());
		try {
			List<Channel> clients = new ArrayList<>();
			for (int i = 0; i < CLIENTS; i++) {
				boolean reads = i % 3 == 0;
				Bootstrap bootstrap = new Bootstrap().group(clientGroup).channel(NioSocketChannel.class).option(ChannelOption.AUTO_READ, reads)
					.option(ChannelOption.SO_RCVBUF, 16 * 1024).handler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) {
							ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 8192, 0, 2, -2, 2, true))
								.addLast(new ChannelInboundHandlerAdapter() {

									@Override
									public void channelRead(io.netty.channel.ChannelHandlerContext ctx, Object msg) {
										ReferenceCountUtil.release(msg);
									}
								});
						}
					});
				clients.add(bootstrap.connect("127.0.0.1", port).syncUninterruptibly().channel());
			}
			long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
			while (connections.size() < CLIENTS && System.nanoTime() < deadline)
				Thread.sleep(10);
			assertEquals(CLIENTS, connections.size());

			try (ExecutorService senders = Executors.newVirtualThreadPerTaskExecutor()) {
				for (int s = 0; s < 4; s++) {
					senders.execute(() -> {
						for (int p = 0; p < PACKETS / 4; p++) {
							AionTransportStressTest.StressPacket packet = new AionTransportStressTest.StressPacket(0, p) {

								@Override
								protected void writeImpl(AionConnection con) {
									writeB(new byte[4000]);
								}
							};
							for (AionConnection c : connections)
								c.sendPacket(packet);
						}
					});
				}
				Thread.sleep(50); // writes are in flight now
				for (int i = 2; i < CLIENTS; i += 3) { // reset while the server writes
					Channel ch = clients.get(i);
					ch.config().setOption(ChannelOption.SO_LINGER, 0);
					ch.close();
				}
			}
			long start = System.nanoTime();
			CompletableFuture<Void> shutdown = CompletableFuture.runAsync(server::shutdown);
			try {
				shutdown.get(20, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				fail(transport + ", " + ioThreads + " I/O thread(s): shutdown didn't return within 20 s");
			}
			System.out.printf("%s, %d I/O thread(s): shutdown took %.1f s%n", transport, ioThreads, (System.nanoTime() - start) / 1e9);
		} finally {
			clientGroup.shutdownGracefully(0, 1, TimeUnit.SECONDS).syncUninterruptibly();
		}
	}
}
