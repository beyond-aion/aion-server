package com.aionemu.gameserver.network.aion;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import java.net.ServerSocket;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.aionemu.commons.network.netty.NettyServer;
import com.aionemu.commons.network.netty.Transport;
import com.aionemu.gameserver.configs.network.NetworkConfig;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.uring.IoUring;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * The real game client pipeline ({@link AionChannelInitializer}, {@link AionConnection}, {@link AionCryptHandler}) on real sockets of each
 * transport: many threads broadcast shared packet instances to all connections at once, as PacketSendUtility does, and every client decrypts and
 * checks every packet. A packet written by two I/O threads at the same time would come out mixed up and fail the checks.
 */
public class AionTransportStressTest {

	private static final byte[] STATIC_KEY = "nKO/WctQ0AVLbpzfBkS6NevDYT8ourG5CRlmdjyJ72aswx4EPq1UgZhFMXH?3iI9".getBytes(StandardCharsets.US_ASCII);
	private static final int OPCODE = 39;
	private static final int CONNECTIONS = Integer.getInteger("stress.connections", 1000);
	private static final int SENDERS = Integer.getInteger("stress.senders", 8);
	private static final int BROADCASTS_PER_SENDER = Integer.getInteger("stress.broadcasts", 200);

	@BeforeAll
	public static void initConfig() {
		NetworkConfig.PACKET_PROCESSOR_MAX_PENDING_PACKETS_PER_CONNECTION = 500;
		NetworkConfig.MAX_PENDING_SEND_KILOBYTES = 4096;
		NetworkConfig.MAX_SEND_STALL_SECONDS = 60;
		NetworkConfig.CLIENT_AUTH_TIMEOUT_SECONDS = 0;
	}

	@TestFactory
	public Stream<DynamicTest> broadcastsArriveIntact() {
		String transports = System.getProperty("stress.transports", "NIO,EPOLL,IO_URING"), threads = System.getProperty("stress.threads", "1,4");
		int repeat = Integer.getInteger("stress.repeat", 1);
		return Stream.of(transports.split(",")).map(Transport::valueOf).flatMap(t -> Stream.of(threads.split(",")).map(Integer::parseInt)
			.flatMap(n -> java.util.stream.IntStream.rangeClosed(1, repeat).mapToObj(r -> DynamicTest.dynamicTest(t + ", " + n + " I/O thread(s), run " + r,
				() -> broadcastsArriveIntact(t, n)))));
	}

	private void broadcastsArriveIntact(Transport transport, int ioThreads) throws Exception {
		Assumptions.assumeTrue(transport != Transport.EPOLL || Epoll.isAvailable(), "epoll is unavailable");
		Assumptions.assumeTrue(transport != Transport.IO_URING || IoUring.isAvailable(), "io_uring is unavailable");
		int port;
		try (ServerSocket s = new ServerSocket(0)) {
			port = s.getLocalPort();
		}
		NettyServer server = new NettyServer(transport, ioThreads);
		List<AionConnection> connections = new ArrayList<>();
		server.bind(new InetSocketAddress("127.0.0.1", port), "stress test clients", channel -> {
			AionConnection connection = new AionConnection(channel);
			AionChannelInitializer.init(channel, connection);
			synchronized (connections) {
				connections.add(connection);
			}
		});
		EventLoopGroup clientGroup = new MultiThreadIoEventLoopGroup(4, NioIoHandler.newFactory());
		int expected = SENDERS * BROADCASTS_PER_SENDER;
		CountDownLatch keyed = new CountDownLatch(CONNECTIONS), done = new CountDownLatch(CONNECTIONS);
		AtomicInteger disconnects = new AtomicInteger();
		AtomicReference<String> firstError = new AtomicReference<>();
		AtomicLong bytes = new AtomicLong();
		try {
			Bootstrap bootstrap = new Bootstrap().group(clientGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch) {
						ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 8192, 0, 2, -2, 2, true))
							.addLast(new Client(expected, keyed, done, disconnects, firstError, bytes));
					}
				});
			List<Channel> clients = new ArrayList<>();
			for (int i = 0; i < CONNECTIONS; i++)
				clients.add(bootstrap.connect("127.0.0.1", port).syncUninterruptibly().channel());
			assertTrue(keyed.await(30, TimeUnit.SECONDS), "not every client got SM_KEY");
			assertEquals(CONNECTIONS, connections.size());

			long start = System.nanoTime();
			try (ExecutorService senders = Executors.newVirtualThreadPerTaskExecutor()) {
				for (int s = 0; s < SENDERS; s++) {
					int sender = s;
					senders.execute(() -> {
						for (int seq = 0; seq < BROADCASTS_PER_SENDER; seq++) {
							StressPacket packet = new StressPacket(sender, seq);
							for (AionConnection c : connections)
								c.sendPacket(packet);
						}
					});
				}
			}
			boolean finished = done.await(Integer.getInteger("stress.wait_seconds", 120), TimeUnit.SECONDS);
			double seconds = (System.nanoTime() - start) / 1e9;
			System.out.printf("%s, %d I/O thread(s): %d connections x %d packets in %.1f s, %.0f MB, first error: %s%n", transport, ioThreads,
				CONNECTIONS, expected, seconds, bytes.get() / 1e6, firstError.get());
			assertNull(firstError.get());
			assertEquals(0, disconnects.get(), "connections were closed");
			assertTrue(finished, "not every client got every packet within 120 s");
			for (Channel ch : clients)
				ch.close();
		} finally {
			clientGroup.shutdownGracefully(0, 1, TimeUnit.SECONDS).syncUninterruptibly();
			long start = System.nanoTime();
			java.util.concurrent.CompletableFuture<Void> shutdown = java.util.concurrent.CompletableFuture.runAsync(server::shutdown);
			try {
				shutdown.get(30, TimeUnit.SECONDS);
				System.out.printf("%s, %d I/O thread(s): shutdown took %.1f s%n", transport, ioThreads, (System.nanoTime() - start) / 1e9);
			} catch (java.util.concurrent.TimeoutException e) {
				System.out.printf("%s, %d I/O thread(s): SHUTDOWN HUNG%n", transport, ioThreads);
			}
		}
	}

	/**
	 * Body: sender, sequence number, a run of bytes and a string whose length and content follow from both, written byte by byte so that two
	 * threads writing it at once would interleave.
	 */
	static class StressPacket extends AionServerPacket {

		final int sender, seq;

		StressPacket(int sender, int seq) {
			super(OPCODE);
			this.sender = sender;
			this.seq = seq;
		}

		static int length(int sender, int seq) {
			return 16 + (sender * 131 + seq * 17) % 400;
		}

		static int byteAt(int sender, int seq, int i) {
			return (sender * 31 + seq * 7 + i) & 0xFF;
		}

		@Override
		protected void writeImpl(AionConnection con) {
			writeD(sender);
			writeD(seq);
			int length = length(sender, seq);
			writeH(length);
			for (int i = 0; i < length; i++)
				writeC(byteAt(sender, seq, i));
			writeS("s" + sender + "#" + seq);
		}
	}

	private static class Client extends SimpleChannelInboundHandler<ByteBuf> {

		private final int expected;
		private final CountDownLatch keyed, done;
		private final AtomicInteger disconnects;
		private final AtomicReference<String> firstError;
		private final AtomicLong bytes;
		private final int[] nextSeq = new int[SENDERS];
		private long serverKey;
		private boolean hasKey;
		private int received;

		Client(int expected, CountDownLatch keyed, CountDownLatch done, AtomicInteger disconnects, AtomicReference<String> firstError, AtomicLong bytes) {
			this.expected = expected;
			this.keyed = keyed;
			this.done = done;
			this.disconnects = disconnects;
			this.firstError = firstError;
			this.bytes = bytes;
		}

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, ByteBuf frame) {
			bytes.addAndGet(frame.readableBytes() + 2);
			if (!hasKey) { // SM_KEY, unencrypted
				frame.skipBytes(5);
				int seed = frame.readIntLE();
				serverKey = (((seed - 0x3FF2CCCF) ^ 0xCD92E4DF) & 0xFFFFFFFFL) | 0x87546CA1L << 32;
				hasKey = true;
				keyed.countDown();
				return;
			}
			byte[] data = ByteBufUtil.getBytes(frame);
			byte prev = 0;
			for (int i = 0; i < data.length; i++) {
				byte cipher = data[i];
				data[i] ^= (byte) (serverKey >>> ((i & 7) * 8));
				if (i > 0)
					data[i] ^= (byte) (STATIC_KEY[i & 63] ^ prev);
				prev = cipher;
			}
			serverKey += data.length;
			String error = check(io.netty.buffer.Unpooled.wrappedBuffer(data));
			if (error != null) {
				if (firstError.compareAndSet(null, error + ": " + ByteBufUtil.hexDump(data)))
					System.err.println("Corrupt packet: " + firstError.get());
				ctx.close();
				return;
			}
			if (++received == expected)
				done.countDown();
		}

		private String check(ByteBuf p) {
			int wireOpcode = p.readUnsignedShortLE();
			if ((wireOpcode ^ 0xDF) - 207 != OPCODE || p.readByte() != 0x44 || p.readUnsignedShortLE() != (~wireOpcode & 0xFFFF))
				return "header";
			int sender = p.readIntLE(), seq = p.readIntLE();
			if (sender < 0 || sender >= SENDERS || seq != nextSeq[sender])
				return "sender " + sender + " seq " + seq;
			nextSeq[sender]++;
			int length = p.readUnsignedShortLE();
			if (length != StressPacket.length(sender, seq))
				return "length";
			for (int i = 0; i < length; i++)
				if ((p.readByte() & 0xFF) != StressPacket.byteAt(sender, seq, i))
					return "byte " + i;
			StringBuilder sb = new StringBuilder();
			for (char c; (c = (char) p.readShortLE()) != 0;)
				sb.append(c);
			if (!sb.toString().equals("s" + sender + "#" + seq) || p.isReadable())
				return "string";
			return null;
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			if (received < expected)
				disconnects.incrementAndGet();
			super.channelInactive(ctx);
		}
	}
}
