package com.aionemu.loginserver.network.ncrypt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HexFormat;

import org.junit.jupiter.api.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * The expected bytes were produced by the byte array implementation the login server used before it moved to Netty. Unlike a round trip through
 * the client side of the cipher, they also catch a mistake that breaks encryption and decryption alike.
 */
public class CryptEngineTest {

	private static final int XOR_KEY = 0x1234ABCD;
	private static final String FIRST_PACKET = "80a5da452f7de4fc2fe5a9c81a35a221d40a2348a821e922d8ddc194ae825caf0f63bf2d7f48f750676cbec071018c120ab1a7b8962a510f";
	private static final String SECOND_PACKET = "84a289050547e7dca5e2c9e315c6ff2499c571619dfdca10";
	private static final String CLIENT_PLAIN = "0b000000112233441a2233441a223344";
	private static final String CLIENT_CIPHER = "f145b9b4084c3cfd7e5c1a0cb8cddfd7";

	private static byte[] sessionKey() {
		byte[] key = new byte[16];
		for (int i = 0; i < 16; i++)
			key[i] = (byte) (0x31 + i * 5);
		return key;
	}

	private static CryptEngine engine() {
		CryptEngine engine = new CryptEngine();
		engine.updateKey(sessionKey());
		return engine;
	}

	private static byte[] firstPlain() {
		byte[] plain = new byte[40];
		for (int i = 0; i < plain.length; i++)
			plain[i] = (byte) (i * 7 + 3);
		return plain;
	}

	private static byte[] secondPlain() {
		byte[] plain = new byte[13];
		for (int i = 0; i < plain.length; i++)
			plain[i] = (byte) (0xF0 - i * 11);
		return plain;
	}

	/** Puts the bytes at the offset of a buffer filled with garbage, like a reused or pooled buffer. */
	private static ByteBuf buffer(boolean direct, int offset, byte[] bytes, int capacity) {
		ByteBuf buf = direct ? Unpooled.directBuffer(capacity, capacity) : Unpooled.buffer(capacity, capacity);
		for (int i = 0; i < capacity; i++)
			buf.setByte(i, 0x5A);
		buf.setBytes(offset, bytes);
		return buf;
	}

	private static byte[] read(ByteBuf buf, int offset, int length) {
		byte[] bytes = new byte[length];
		buf.getBytes(offset, bytes);
		return bytes;
	}

	@Test
	public void encryptsKnownVectors() {
		for (boolean direct : new boolean[] { false, true }) {
			for (int offset : new int[] { 0, 2, 7 }) {
				CryptEngine engine = engine();
				ByteBuf first = buffer(direct, offset, firstPlain(), offset + 64);
				int firstLength = engine.encrypt(first, offset, 40, XOR_KEY);
				assertEquals(56, firstLength);
				assertEquals(FIRST_PACKET, HexFormat.of().formatHex(read(first, offset, firstLength)));

				ByteBuf second = buffer(direct, offset, secondPlain(), offset + 32);
				int secondLength = engine.encrypt(second, offset, 13, XOR_KEY);
				assertEquals(24, secondLength);
				assertEquals(SECOND_PACKET, HexFormat.of().formatHex(read(second, offset, secondLength)));
			}
		}
	}

	@Test
	public void decryptsAndAcceptsAValidClientPacket() {
		for (boolean direct : new boolean[] { false, true }) {
			for (int offset : new int[] { 0, 2, 7 }) {
				CryptEngine engine = engine();
				engine.encrypt(Unpooled.buffer(64, 64), 0, 40, XOR_KEY); // SM_INIT switches the engine to the session key
				ByteBuf packet = buffer(direct, offset, HexFormat.of().parseHex(CLIENT_CIPHER), offset + 16);
				assertTrue(engine.decrypt(packet, offset, 16));
				assertArrayEquals(HexFormat.of().parseHex(CLIENT_PLAIN), read(packet, offset, 16));
			}
		}
	}

	@Test
	public void rejectsACorruptedClientPacket() {
		CryptEngine engine = engine();
		engine.encrypt(Unpooled.buffer(64, 64), 0, 40, XOR_KEY);
		byte[] corrupted = HexFormat.of().parseHex(CLIENT_CIPHER);
		corrupted[3] ^= 0x40;
		assertFalse(engine.decrypt(Unpooled.wrappedBuffer(corrupted), 0, 16));
	}
}
