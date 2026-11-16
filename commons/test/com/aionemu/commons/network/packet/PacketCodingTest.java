package com.aionemu.commons.network.packet;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HexFormat;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

/**
 * Pins down the little-endian wire format of the packet read and write methods.
 */
public class PacketCodingTest {

	private static final String WIRE = "44332211" + "b2a1" + "f0" + "0807060504030201" + "0000c03f" + "00000000000002c0" + "410062000000" + "0000"
		+ "0908";

	private static class ServerPacket extends BaseServerPacket {

		void write(ByteBuf out) {
			setBuf(out);
			writeD(0x11223344);
			writeH(0xA1B2);
			writeC(0xF0);
			writeQ(0x0102030405060708L);
			writeF(1.5f);
			writeDF(-2.25);
			writeS("Ab");
			writeS(null);
			writeB(new byte[] { 9, 8 });
		}
	}

	private static class ClientPacket extends BaseClientPacket<Object> {

		private final Consumer<ClientPacket> reader;

		ClientPacket(byte[] data, Consumer<ClientPacket> reader) {
			super(Unpooled.wrappedBuffer(data), 1);
			this.reader = reader;
		}

		@Override
		protected void readImpl() {
			reader.accept(this);
		}

		@Override
		protected void runImpl() {
		}

		@Override
		public void run() {
		}
	}

	private static boolean read(byte[] data, Consumer<ClientPacket> reader) {
		return new ClientPacket(data, reader).read();
	}

	@Test
	public void writesLittleEndian() {
		ByteBuf out = Unpooled.directBuffer(4, 64);
		new ServerPacket().write(out);
		assertEquals(WIRE, HexFormat.of().formatHex(ByteBufUtil.getBytes(out)));
	}

	@Test
	public void readsLittleEndian() {
		assertTrue(read(HexFormat.of().parseHex(WIRE), p -> {
			assertEquals(0x11223344, p.readD());
			assertEquals((short) 0xA1B2, p.readH());
			assertEquals(0xF0, p.readUC());
			assertEquals(0x0102030405060708L, p.readQ());
			assertEquals(1.5f, p.readF());
			assertEquals(-2.25, p.readDF());
			assertEquals("Ab", p.readS());
			assertEquals("", p.readS());
			assertArrayEquals(new byte[] { 9, 8 }, p.readB(2));
		}));
	}

	@Test
	public void failsOnAPacketThatIsTooShort() {
		assertTrue(read(new byte[] { 1, 2, 3, 4 }, ClientPacket::readD));
		assertFalse(read(new byte[] { 1, 2, 3 }, ClientPacket::readD));
		assertFalse(read(new byte[] { 1, 2, 3 }, p -> p.readB(4)));
		assertFalse(read(new byte[] { 1, 2, 3 }, p -> p.readB(-1)));
		assertFalse(read(new byte[] { 'A', 0, 'B', 0 }, ClientPacket::readS), "string without terminator");
	}
}
