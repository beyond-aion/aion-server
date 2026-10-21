package com.aionemu.loginserver.network.aion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.aionemu.commons.network.netty.NettyPipeline;
import com.aionemu.loginserver.network.ncrypt.BlowfishCipher;
import com.aionemu.loginserver.network.ncrypt.KeyGen;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

/**
 * Runs the start of a login through the real pipeline and packets, playing the client's side of the encryption: SM_INIT under the static key and
 * the XOR pass, then CM_AUTH_GG and SM_AUTH_GG under the session key.
 */
public class LoginPipelineTest {

	private static final byte[] STATIC_KEY = { (byte) 0x6b, (byte) 0x60, (byte) 0xcb, (byte) 0x5b, (byte) 0x82, (byte) 0xce, (byte) 0x90, (byte) 0xb1,
		(byte) 0xcc, (byte) 0x2b, (byte) 0x6c, (byte) 0x55, (byte) 0x6c, (byte) 0x6c, (byte) 0x6c, (byte) 0x6c };

	@BeforeAll
	public static void initKeys() {
		KeyGen.init();
	}

	private static EmbeddedChannel channel() throws Exception {
		InetSocketAddress client = new InetSocketAddress("127.0.0.1", 50000);
		EmbeddedChannel channel = new EmbeddedChannel(false, false) {

			@Override
			protected SocketAddress remoteAddress0() {
				return client;
			}
		};
		NettyPipeline.init(channel, new LoginConnection(channel, Runnable::run), 8192 * 2);
		channel.register();
		return channel;
	}

	/** Reads one server packet and returns its body (opcode and data, without the size), decrypted. */
	private static ByteBuffer readServerPacket(EmbeddedChannel channel, byte[] key, boolean first) {
		ByteBuf out = channel.readOutbound();
		assertNotNull(out, "no packet was sent");
		int size = out.readUnsignedShortLE();
		assertEquals(size - 2, out.readableBytes());
		ByteBuffer body = ByteBuffer.allocate(size - 2).order(ByteOrder.LITTLE_ENDIAN);
		out.readBytes(body);
		out.release();
		new BlowfishCipher(key).decipher(body, 0, body.capacity());
		if (first) {
			decXORPass(body);
		} else {
			int checksum = 0;
			for (int i = 0; i < body.capacity() - 4; i += 4)
				checksum ^= body.getInt(i);
			assertEquals(checksum, body.getInt(body.capacity() - 4), "server checksum");
		}
		return body;
	}

	/** Reverses the XOR pass over the first packet: the last word before the final block holds the running key. */
	private static void decXORPass(ByteBuffer body) {
		int stop = body.capacity() - 8;
		int ecx = body.getInt(stop);
		for (int pos = stop - 4; pos >= 4; pos -= 4) {
			int d = body.getInt(pos) ^ ecx;
			body.putInt(pos, d);
			ecx -= d;
		}
	}

	/**
	 * Builds a client packet the way the 4.8 client does: data, padding to whole blocks, a checksum word that makes all words before it XOR to zero,
	 * one more word, then encryption.
	 */
	private static ByteBuf clientPacket(byte[] data, byte[] key, boolean corrupt) {
		int padded = (data.length + 7) & ~7;
		int length = padded + 8;
		ByteBuffer body = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
		body.put(data);
		for (int i = data.length; i < padded; i++)
			body.put(i, (byte) 0x5A); // the client leaves stack garbage there
		int checksum = 0;
		for (int i = 0; i < padded; i += 4)
			checksum ^= body.getInt(i);
		body.putInt(padded, checksum);
		body.putInt(padded + 4, 0x12345678);
		if (corrupt)
			body.put(1, (byte) (body.get(1) ^ 0x40));
		new BlowfishCipher(key).cipher(body, 0, length);
		ByteBuf frame = Unpooled.buffer(length + 2);
		frame.writeShortLE(length + 2);
		frame.writeBytes(body.array());
		return frame;
	}

	private static byte[] cmAuthGG(int sessionId) {
		ByteBuffer data = ByteBuffer.allocate(21).order(ByteOrder.LITTLE_ENDIAN);
		data.put((byte) 0x07).putInt(sessionId);
		return data.array();
	}

	@Test
	public void sendsInitAndAnswersAuthGG() throws Exception {
		EmbeddedChannel channel = channel();

		ByteBuffer init = readServerPacket(channel, STATIC_KEY, true);
		assertEquals(0x00, init.get(0));
		int sessionId = init.getInt(1);
		assertEquals(0x0000c621, init.getInt(5), "protocol revision");
		byte[] sessionKey = Arrays.copyOfRange(init.array(), 1 + 4 + 4 + 128 + 16, 1 + 4 + 4 + 128 + 16 + 16);

		channel.writeInbound(clientPacket(cmAuthGG(sessionId), sessionKey, false));
		ByteBuffer authGG = readServerPacket(channel, sessionKey, false);
		assertEquals(0x0b, authGG.get(0));
		assertTrue(channel.isOpen());
		channel.finishAndReleaseAll();
	}

	@Test
	public void closesOnAWrongChecksum() throws Exception {
		EmbeddedChannel channel = channel();
		ByteBuffer init = readServerPacket(channel, STATIC_KEY, true);
		byte[] sessionKey = Arrays.copyOfRange(init.array(), 1 + 4 + 4 + 128 + 16, 1 + 4 + 4 + 128 + 16 + 16);

		channel.writeInbound(clientPacket(cmAuthGG(init.getInt(1)), sessionKey, true));
		assertNull(channel.readOutbound(), "a packet with a wrong checksum must not be answered");
		assertFalse(channel.isOpen());
		channel.finishAndReleaseAll();
	}
}
