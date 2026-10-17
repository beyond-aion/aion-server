package com.aionemu.commons.network.netty;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
			getConnection().executed.add(value);
		}

		@Override
		public void run() {
			runImpl();
		}
	}

	private static class TestConnection extends NettyConnection<TestServerPacket> {

		private final Queue<Runnable> tasks;
		private final List<Integer> executed = new ArrayList<>();
		private boolean readFailureFatal = true;
		private boolean disconnected;

		TestConnection(EmbeddedChannel channel, Queue<Runnable> tasks) {
			super(channel, tasks::add);
			this.tasks = tasks;
		}

		void runTasks() {
			for (Runnable task; (task = tasks.poll()) != null;)
				task.run();
		}

		@Override
		protected BaseClientPacket<?> createPacket(ByteBuf data) {
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
			disconnected = true;
		}
	}

	@BeforeEach
	public void connect() throws Exception {
		InetSocketAddress client = new InetSocketAddress("127.0.0.1", 50000);
		channel = new EmbeddedChannel(false, false) {

			@Override
			protected SocketAddress remoteAddress0() {
				return client;
			}
		};
		connection = new TestConnection(channel, new ArrayDeque<>());
		NettyPipeline.init(channel, connection, MAX_PACKET_SIZE);
		channel.register();
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
