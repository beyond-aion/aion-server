package com.aionemu.gameserver.network.aion;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AFTER_TIME_CHECK_4_7_5;
import com.aionemu.gameserver.network.aion.serverpackets.SM_KEY;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TIME_CHECK;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

/**
 * Runs Aion client traffic through the real game server pipeline. The client side of the protocol (frame, opcode obfuscation, key derivation and
 * cipher) is implemented here independently of the server's {@code Crypt}, following the protocol description, so a mistake shared by both
 * directions of the server code still fails the tests.
 */
public class AionPipelineTest {

	private static final byte[] STATIC_KEY = "nKO/WctQ0AVLbpzfBkS6NevDYT8ourG5CRlmdjyJ72aswx4EPq1UgZhFMXH?3iI9".getBytes(StandardCharsets.US_ASCII);
	private static final int CM_TIME_CHECK = 18;

	private final Queue<Runnable> packetTasks = new ArrayDeque<>();
	private EmbeddedChannel channel;
	private AionConnection connection;
	private long serverKey, clientKey;

	@BeforeAll
	public static void initConfig() {
		NetworkConfig.PACKET_PROCESSOR_MAX_PENDING_PACKETS_PER_CONNECTION = 500;
		NetworkConfig.MAX_PENDING_SEND_KILOBYTES = 4096;
		NetworkConfig.MAX_SEND_STALL_SECONDS = 60;
		NetworkConfig.CLIENT_AUTH_TIMEOUT_SECONDS = 60;
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
		connection = new AionConnection(channel, packetTasks::add);
		AionChannelInitializer.init(channel, connection);
		channel.register();
		channel.runPendingTasks();

		ByteBuf key = channel.readOutbound();
		assertNotNull(key, "SM_KEY was not sent");
		int size = key.readUnsignedShortLE();
		assertEquals(size - 2, key.readableBytes());
		assertEquals(ServerPacketsOpcodes.getOpcode(SM_KEY.class), (key.readUnsignedShortLE() ^ 0xDF) - 207);
		assertEquals(0x44, key.readByte(), "SM_KEY is sent unencrypted");
		key.skipBytes(2);
		int seed = key.readIntLE();
		key.release();
		long key32 = ((seed - 0x3FF2CCCF) ^ 0xCD92E4DF) & 0xFFFFFFFFL;
		serverKey = clientKey = key32 | 0x87546CA1L << 32;
	}

	@AfterEach
	public void close() {
		channel.finishAndReleaseAll();
	}

	/**
	 * @return The opcode and body of the next server packet, decrypted.
	 */
	private ServerPacket readServerPacket() {
		channel.runPendingTasks();
		ByteBuf out;
		while ((out = channel.readOutbound()) != null && !out.isReadable()) // a dropped packet leaves an empty write
			out.release();
		assertNotNull(out, "no packet was sent");
		int size = out.readUnsignedShortLE();
		assertEquals(size - 2, out.readableBytes());
		byte[] data = new byte[size - 2];
		out.readBytes(data);
		out.release();
		byte prev = 0;
		for (int i = 0; i < data.length; i++) {
			byte cipher = data[i];
			data[i] ^= (byte) (serverKey >>> ((i & 7) * 8));
			if (i > 0)
				data[i] ^= (byte) (STATIC_KEY[i & 63] ^ prev);
			prev = cipher;
		}
		serverKey += data.length;
		ByteBuf plain = Unpooled.wrappedBuffer(data);
		int wireOpcode = plain.readUnsignedShortLE();
		assertEquals(0x44, plain.readByte(), "server packet marker");
		assertEquals(~wireOpcode & 0xFFFF, plain.readUnsignedShortLE(), "complement of the opcode");
		return new ServerPacket((wireOpcode ^ 0xDF) - 207, plain);
	}

	private record ServerPacket(int opcode, ByteBuf body) {}

	private ByteBuf clientPacket(int opcode, byte[] body) {
		int wireOpcode = ((((opcode + 207) ^ 0xEF) + 0xC) ^ 0xEF) & 0xFFFF;
		ByteBuf plain = Unpooled.buffer().writeShortLE(wireOpcode).writeByte(0x65).writeShortLE(~wireOpcode).writeBytes(body);
		byte[] data = new byte[plain.readableBytes()];
		plain.readBytes(data);
		for (int i = 0; i < data.length; i++) {
			data[i] ^= (byte) (clientKey >>> ((i & 7) * 8));
			if (i > 0)
				data[i] ^= (byte) (STATIC_KEY[i & 63] ^ data[i - 1]);
		}
		clientKey += data.length;
		return frame(data);
	}

	private static ByteBuf frame(byte[] data) {
		return Unpooled.buffer().writeShortLE(data.length + 2).writeBytes(data);
	}

	private static byte[] timeCheckBody(int nanoTime) {
		return ByteBufUtil.getBytes(Unpooled.buffer(4).writeIntLE(nanoTime));
	}

	private void runPackets() {
		for (Runnable task; (task = packetTasks.poll()) != null;)
			task.run();
	}

	private static class TestPacket extends AionServerPacket {

		private final int bodySize;
		private final boolean fail;

		TestPacket(int bodySize, boolean fail) {
			super(39);
			this.bodySize = bodySize;
			this.fail = fail;
		}

		@Override
		protected void writeImpl(AionConnection con) {
			writeB(new byte[bodySize]);
			if (fail)
				throw new IllegalStateException("test");
		}
	}

	@Test
	public void decryptsClientPacketsAndEncryptsTheAnswers() {
		for (int nanoTime : new int[] { 12345, -7 }) {
			channel.writeInbound(clientPacket(CM_TIME_CHECK, timeCheckBody(nanoTime)));
			assertEquals(1, packetTasks.size(), "the packet was not queued for execution");
			runPackets();
			assertEquals(ServerPacketsOpcodes.getOpcode(SM_AFTER_TIME_CHECK_4_7_5.class), readServerPacket().opcode());
			ServerPacket timeCheck = readServerPacket();
			assertEquals(ServerPacketsOpcodes.getOpcode(SM_TIME_CHECK.class), timeCheck.opcode());
			timeCheck.body().skipBytes(4); // server up time
			assertEquals(nanoTime, timeCheck.body().readIntLE());
		}
		assertTrue(channel.isOpen());
	}

	@Test
	public void dropsOversizedServerPacketsWithoutLosingTheCipherState() {
		connection.sendPacket(new TestPacket(AionServerPacket.MAX_CLIENT_SUPPORTED_PACKET_SIZE, false));
		connection.sendPacket(new TestPacket(AionServerPacket.MAX_USABLE_PACKET_BODY_SIZE, false));
		ServerPacket packet = readServerPacket();
		assertEquals(39, packet.opcode());
		assertEquals(AionServerPacket.MAX_USABLE_PACKET_BODY_SIZE, packet.body().readableBytes());
		assertNull(channel.readOutbound());
		assertTrue(channel.isOpen());
	}

	@Test
	public void dropsServerPacketsThatFailToWriteWithoutLosingTheCipherState() {
		connection.sendPacket(new TestPacket(10, true));
		connection.sendPacket(new TestPacket(3, false));
		assertEquals(3, readServerPacket().body().readableBytes());
		assertTrue(channel.isOpen());
	}

	@Test
	public void writesTheSamePacketInstanceAgain() {
		AionServerPacket packet = new TestPacket(100, false);
		connection.sendPacket(packet);
		connection.sendPacket(packet);
		assertEquals(100, readServerPacket().body().readableBytes());
		assertEquals(100, readServerPacket().body().readableBytes());
	}

	@Test
	public void ignoresAPacketThatIsTooShort() {
		channel.writeInbound(clientPacket(CM_TIME_CHECK, new byte[2]));
		assertTrue(packetTasks.isEmpty());
		assertTrue(channel.isOpen());
		channel.writeInbound(clientPacket(CM_TIME_CHECK, timeCheckBody(1)));
		assertEquals(1, packetTasks.size(), "the cipher state got out of sync");
	}

	@Test
	public void ignoresUnknownOpcodes() {
		channel.writeInbound(clientPacket(1, new byte[8]));
		assertTrue(packetTasks.isEmpty());
		assertTrue(channel.isOpen());
	}

	@Test
	public void ignoresPacketsEncryptedForThePreviousConnectionBeforeTheFirstValidOne() {
		byte[] garbage = new byte[16];
		for (int i = 0; i < 5; i++)
			channel.writeInbound(frame(garbage));
		assertTrue(channel.isOpen());
		channel.writeInbound(clientPacket(CM_TIME_CHECK, timeCheckBody(1)));
		assertEquals(1, packetTasks.size(), "the cipher state got out of sync");
		assertTrue(channel.isOpen());
	}

	@Test
	public void closesAfterMorePacketsForThePreviousConnectionThanTheClientCanHold() {
		byte[] garbage = new byte[8190];
		for (int i = 0; i < 8; i++) // 65,536 bytes, the client's send buffer holds 66,560
			channel.writeInbound(frame(garbage));
		assertTrue(channel.isOpen());
		for (int i = 0; i < 3; i++)
			channel.writeInbound(frame(garbage));
		assertFalse(channel.isOpen());
	}

	@Test
	public void closesAfterThreePacketsThatCantBeDecrypted() {
		channel.writeInbound(clientPacket(CM_TIME_CHECK, timeCheckBody(1)));
		byte[] garbage = new byte[16];
		channel.writeInbound(frame(garbage));
		channel.writeInbound(frame(garbage));
		assertTrue(channel.isOpen());
		channel.writeInbound(frame(garbage));
		assertFalse(channel.isOpen());
	}

	@Test
	public void closesOnAnEmptyPacket() {
		channel.writeInbound(Unpooled.buffer().writeShortLE(2));
		assertFalse(channel.isOpen());
	}

	@Test
	public void closesOnASizeHeaderBelowItsOwnSize() {
		channel.writeInbound(Unpooled.buffer().writeShortLE(1).writeZero(8));
		assertFalse(channel.isOpen());
	}

	@Test
	public void closesOnAPacketLargerThanTheLimitBeforeReceivingIt() {
		channel.writeInbound(Unpooled.buffer().writeShortLE(AionChannelInitializer.MAX_RECEIVED_PACKET_SIZE + 1).writeZero(16));
		assertFalse(channel.isOpen());
	}

	@Test
	public void closesWhenTooManyPacketsWaitForExecution() {
		int maxPending = NetworkConfig.PACKET_PROCESSOR_MAX_PENDING_PACKETS_PER_CONNECTION;
		NetworkConfig.PACKET_PROCESSOR_MAX_PENDING_PACKETS_PER_CONNECTION = 2;
		try {
			for (int i = 0; i < 2; i++)
				channel.writeInbound(clientPacket(CM_TIME_CHECK, timeCheckBody(i)));
			assertTrue(channel.isOpen());
			channel.writeInbound(clientPacket(CM_TIME_CHECK, timeCheckBody(2)));
			assertFalse(channel.isOpen());
		} finally {
			NetworkConfig.PACKET_PROCESSOR_MAX_PENDING_PACKETS_PER_CONNECTION = maxPending;
		}
	}

	@Test
	public void closesWhenTheClientDoesntAuthenticateInTime() {
		channel.advanceTimeBy(NetworkConfig.CLIENT_AUTH_TIMEOUT_SECONDS - 1, TimeUnit.SECONDS);
		channel.runScheduledPendingTasks();
		assertTrue(channel.isOpen());
		channel.advanceTimeBy(1, TimeUnit.SECONDS);
		channel.runScheduledPendingTasks();
		assertFalse(channel.isOpen());
	}

	@Test
	public void staysOpenAfterAuthentication() {
		connection.setState(AionConnection.State.AUTHED);
		channel.advanceTimeBy(NetworkConfig.CLIENT_AUTH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		channel.runScheduledPendingTasks();
		assertTrue(channel.isOpen());
	}
}
