package com.aionemu.commons.network.netty;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aionemu.commons.network.packet.BaseClientPacket;
import com.aionemu.commons.network.packet.BaseServerPacket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;

public class NettyConnectionTest {

	private static final int MAX_PACKET_SIZE = 64;

	private EmbeddedChannel channel;
	private TestConnection connection;

	private static class TestServerPacket extends BaseServerPacket {

		private final int bodySize;
		private final boolean fail;

		TestServerPacket(int bodySize, boolean fail) {
			super(1);
			this.bodySize = bodySize;
			this.fail = fail;
		}

		void write(ByteBuf out) {
			setBuf(out);
			int start = buf.writerIndex();
			writeH(0);
			for (int i = 0; i < bodySize; i++) {
				writeC(i);
				if (i % 16 == 0)
					Thread.yield(); // gives other threads writing the same packet a chance to interfere
			}
			if (fail)
				throw new IllegalStateException("test");
			buf.setShortLE(start, buf.writerIndex() - start);
		}
	}

	private static class TestClientPacket extends BaseClientPacket<TestConnection> {

		private int value;

		TestClientPacket(ByteBuf buf) {
			super(buf, 1);
		}

		@Override
		protected void readImpl() {
			value = readD();
		}

		@Override
		protected void runImpl() {
			getConnection().packetAction.accept(value);
			getConnection().executed.add(value);
		}

		@Override
		public void run() {
			runImpl();
		}
	}

	private static class TestConnection extends NettyConnection<TestServerPacket> {

		private final Queue<Runnable> tasks;
		private final List<Integer> executed = new CopyOnWriteArrayList<>();
		private final AtomicInteger disconnects = new AtomicInteger();
		private final CountDownLatch disconnectLatch = new CountDownLatch(1);
		private volatile IntConsumer packetAction = _ -> {};
		private volatile List<Integer> executedBeforeDisconnect;
		private boolean readFailureFatal = true;
		private volatile boolean disconnected;
		private int createdPackets;

		TestConnection(EmbeddedChannel channel, Queue<Runnable> tasks) {
			super(channel, tasks::add);
			this.tasks = tasks;
		}

		TestConnection(EmbeddedChannel channel, Executor packetExecutor) {
			super(channel, packetExecutor);
			this.tasks = null;
		}

		void runTasks() {
			for (Runnable task; (task = tasks.poll()) != null;)
				task.run();
		}

		@Override
		protected BaseClientPacket<?> createPacket(ByteBuf data) {
			createdPackets++;
			TestClientPacket packet = new TestClientPacket(data);
			packet.setConnection(this);
			return packet;
		}

		@Override
		protected void writePacket(TestServerPacket packet, ByteBuf buffer) {
			packet.write(buffer);
		}

		@Override
		protected boolean isReadFailureFatal() {
			return readFailureFatal;
		}

		@Override
		protected int getMaxPendingPackets() {
			return 3;
		}

		@Override
		protected void onConnect() {
		}

		@Override
		protected void onDisconnect() {
			executedBeforeDisconnect = List.copyOf(executed);
			disconnected = true;
			disconnects.incrementAndGet();
			disconnectLatch.countDown();
		}
	}

	@BeforeEach
	public void connect() throws Exception {
		channel = newChannel();
		connection = new TestConnection(channel, new ArrayDeque<>());
		NettyPipeline.init(channel, connection, MAX_PACKET_SIZE);
		channel.register();
	}

	private static EmbeddedChannel newChannel() {
		InetSocketAddress client = new InetSocketAddress("127.0.0.1", 50000);
		return new EmbeddedChannel(false, false) {

			@Override
			protected SocketAddress remoteAddress0() {
				return client;
			}
		};
	}

	/**
	 * @return A connection whose packets are executed concurrently, like those of the connections between the servers.
	 */
	private static TestConnection connectWithConcurrentPackets(Executor packetExecutor) throws Exception {
		EmbeddedChannel channel = newChannel();
		TestConnection connection = new TestConnection(channel, packetExecutor);
		NettyPipeline.init(channel, connection, MAX_PACKET_SIZE);
		channel.register();
		return connection;
	}

	@AfterEach
	public void close() {
		channel.finishAndReleaseAll();
	}

	private static ByteBuf clientPacket(int value) {
		return Unpooled.buffer().writeShortLE(6).writeIntLE(value);
	}

	/**
	 * @return The bodies of the sent packets, without empty writes.
	 */
	private List<byte[]> sentPackets() {
		List<byte[]> packets = new ArrayList<>();
		for (ByteBuf out; (out = channel.readOutbound()) != null;) {
			if (out.isReadable()) {
				assertEquals(out.readableBytes(), out.readUnsignedShortLE());
				packets.add(ByteBufUtil.getBytes(out));
			}
			out.release();
		}
		return packets;
	}

	@Test
	public void executesPacketsInOrder() {
		for (int i = 0; i < 3; i++)
			channel.writeInbound(clientPacket(i));
		connection.runTasks();
		assertEquals(List.of(0, 1, 2), connection.executed);
	}

	@Test
	public void closesWhenTooManyPacketsWaitForExecution() {
		for (int i = 0; i < 3; i++)
			channel.writeInbound(clientPacket(i));
		assertTrue(channel.isOpen());
		channel.writeInbound(clientPacket(3));
		assertFalse(channel.isOpen());
		connection.runTasks();
		assertEquals(List.of(0, 1, 2), connection.executed);
		assertTrue(connection.disconnected, "onDisconnect must run after the pending packets");
	}

	@Test
	public void disconnectsAConnectionWithoutPendingPackets() {
		channel.close();
		assertFalse(connection.disconnected, "onDisconnect must run on the packet executor");
		connection.runTasks();
		assertEquals(1, connection.disconnects.get());
	}

	@Test
	public void disconnectsAfterThePacketsThatAreStillRunningConcurrently() throws Exception {
		try (ExecutorService threads = Executors.newCachedThreadPool()) {
			TestConnection connection = connectWithConcurrentPackets(threads);
			CountDownLatch running = new CountDownLatch(2);
			CountDownLatch finish = new CountDownLatch(1);
			connection.packetAction = _ -> {
				running.countDown();
				try {
					finish.await();
				} catch (InterruptedException e) {
					throw new IllegalStateException(e);
				}
			};
			EmbeddedChannel channel = (EmbeddedChannel) connection.getChannel();
			try {
				channel.writeInbound(clientPacket(1));
				channel.writeInbound(clientPacket(2));
				assertTrue(running.await(5, TimeUnit.SECONDS));
				channel.close();
				// an idle thread of the pool would run onDisconnect right away, if it was just queued behind the packets
				assertFalse(connection.disconnectLatch.await(200, TimeUnit.MILLISECONDS), "onDisconnect ran while packets were still running");
			} finally {
				finish.countDown(); // also if the test fails, or the packet threads would never end
			}
			assertTrue(connection.disconnectLatch.await(5, TimeUnit.SECONDS));
			assertEquals(2, connection.executedBeforeDisconnect.size());
			assertEquals(1, connection.disconnects.get());
			channel.finishAndReleaseAll();
		}
	}

	@Test
	public void disconnectsOnceWhenTheLastPacketFinishesWhileTheChannelCloses() throws Exception {
		List<TestConnection> connections = new ArrayList<>();
		try (ExecutorService threads = Executors.newFixedThreadPool(4)) {
			for (int i = 0; i < 3000; i++) {
				TestConnection connection = connectWithConcurrentPackets(threads);
				connections.add(connection);
				EmbeddedChannel channel = (EmbeddedChannel) connection.getChannel();
				int packets = 1 + i % 3;
				for (int value = 0; value < packets; value++)
					channel.writeInbound(clientPacket(value));
				if (i % 2 == 0)
					Thread.yield();
				channel.close(); // races with the end of the packets
				assertTrue(connection.disconnectLatch.await(5, TimeUnit.SECONDS), "onDisconnect didn't run");
				assertEquals(packets, connection.executedBeforeDisconnect.size(), "onDisconnect ran before all packets were executed");
				channel.finishAndReleaseAll();
			}
		}
		for (TestConnection connection : connections) // all packet threads have finished, so a second onDisconnect would have happened by now
			assertEquals(1, connection.disconnects.get());
	}

	@Test
	public void ignoresThePacketsThatFollowInTheSameDataAfterClosing() {
		ByteBuf data = Unpooled.buffer();
		for (int i = 0; i < 100; i++)
			data.writeShortLE(6).writeIntLE(i);
		channel.writeInbound(data);
		assertFalse(channel.isOpen());
		assertEquals(4, connection.createdPackets, "the fourth packet exceeds the limit of waiting packets and closes the channel");
	}

	@Test
	public void closesOnAPacketThatIsTooShortIfReadFailuresAreFatal() {
		channel.writeInbound(Unpooled.buffer().writeShortLE(4).writeShortLE(1));
		assertFalse(channel.isOpen());
	}

	@Test
	public void ignoresAPacketThatIsTooShortOtherwise() {
		connection.readFailureFatal = false;
		channel.writeInbound(Unpooled.buffer().writeShortLE(4).writeShortLE(1));
		channel.writeInbound(clientPacket(7));
		assertTrue(channel.isOpen());
		connection.runTasks();
		assertEquals(List.of(7), connection.executed);
	}

	@Test
	public void closesOnAnEmptyPacket() {
		channel.writeInbound(Unpooled.buffer().writeShortLE(2));
		assertFalse(channel.isOpen());
	}

	@Test
	public void closesOnAPacketLargerThanTheLimit() {
		channel.writeInbound(Unpooled.buffer().writeShortLE(MAX_PACKET_SIZE + 1).writeZero(4));
		assertFalse(channel.isOpen());
	}

	@Test
	public void dropsPacketsThatAreTooLargeOrFailToWrite() {
		connection.sendPacket(new TestServerPacket(MAX_PACKET_SIZE - 1, false));
		connection.sendPacket(new TestServerPacket(3, true));
		connection.sendPacket(new TestServerPacket(MAX_PACKET_SIZE - 2, false));
		List<byte[]> sent = sentPackets();
		assertEquals(1, sent.size());
		assertEquals(MAX_PACKET_SIZE - 2, sent.getFirst().length);
		assertTrue(channel.isOpen());
	}

	@Test
	public void sendsPendingPacketsBeforeClosingAndDropsLaterOnes() {
		connection.sendPacket(new TestServerPacket(1, false));
		connection.close();
		connection.sendPacket(new TestServerPacket(2, false));
		connection.close(new TestServerPacket(3, false));
		assertEquals(1, sentPackets().size());
		assertFalse(channel.isOpen());
	}

	@Test
	public void closesAfterATimeoutIfTheLastPacketCantBeSent() {
		channel.pipeline().addFirst(new ChannelOutboundHandlerAdapter() {

			@Override
			public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
				// like a client that doesn't read: the write never completes
			}
		});
		connection.close(new TestServerPacket(1, false));
		channel.runPendingTasks();
		assertTrue(channel.isOpen());
		channel.advanceTimeBy(2, TimeUnit.SECONDS);
		channel.runScheduledPendingTasks();
		assertFalse(channel.isOpen());
	}

	@Test
	public void writesAPacketSharedByConnectionsOfDifferentThreadsCorrectly() throws Exception {
		TestServerPacket shared = new TestServerPacket(4000, false);
		ByteBuf expected = Unpooled.buffer();
		connection.encode(shared, expected);
		try (ExecutorService threads = Executors.newFixedThreadPool(8)) {
			List<Future<ByteBuf>> results = new ArrayList<>();
			for (int i = 0; i < 64; i++) {
				results.add(threads.submit(() -> {
					ByteBuf out = Unpooled.buffer();
					connection.encode(shared, out);
					return out;
				}));
			}
			for (Future<ByteBuf> result : results)
				assertEquals(expected, result.get());
		}
	}
}
