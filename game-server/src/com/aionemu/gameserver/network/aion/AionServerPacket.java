package com.aionemu.gameserver.network.aion;

import com.aionemu.commons.network.packet.BaseServerPacket;
import com.aionemu.gameserver.network.Crypt;

import io.netty.buffer.ByteBuf;

/**
 * Base class for every GS -> Aion Server Packet.
 * 
 * @author -Nemesiss-
 */
public abstract class AionServerPacket extends BaseServerPacket {

	public static final int MAX_CLIENT_SUPPORTED_PACKET_SIZE = 8192;
	public static final int MAX_USABLE_PACKET_BODY_SIZE = MAX_CLIENT_SUPPORTED_PACKET_SIZE - 7; // 8192 - 2 (body length) - 2 (opCode) - 1 (staticServerPacketCode) - 2 (opCode flipped bits)

	public static int byteLengthForString(String text) {
		if (text == null || text.isEmpty())
			return 2;
		return (text.length() + 1) * 2;
	}

	public static int byteLengthForFixedString(int fixedLength) {
		return (fixedLength + 1) * 2;
	}

	/**
	 * Constructs new server packet
	 */
	protected AionServerPacket() {
		super();
		setOpCode(ServerPacketsOpcodes.getOpcode(getClass()));
	}

	protected AionServerPacket(int opCode) {
		super(opCode);
	}

	/**
	 * Write packet opCode and two additional bytes
	 */
	private void writeOP() {
		// obfuscate packet id
		int op = Crypt.encodeServerPacketOpcode(getOpCode());
		buf.writeShortLE(op);
		// put static server packet code
		buf.writeByte(Crypt.staticServerPacketCode);
		// for checksum?
		buf.writeShortLE(~op);
	}

	/**
	 * Write this packet data for given connection to the end of the buffer. The encryption happens afterwards, in the channel pipeline.
	 * 
	 * @param con
	 * @param buffer
	 */
	public final void write(AionConnection con, ByteBuf buffer) {
		setBuf(buffer);
		int start = buf.writerIndex();
		buf.writeShortLE(0);
		writeOP();
		writeImpl(con);
		buf.setShortLE(start, buf.writerIndex() - start);
	}

	/**
	 * Write data that this packet represents to given byte buffer.
	 * 
	 * @param con
	 */
	protected void writeImpl(AionConnection con) {

	}

	public final ByteBuf getBuf() {
		return this.buf;
	}

	/**
	 * Write string to buffer with a fixed length. Characters exceeding fixedLength will be truncated. Missing ones will be zero-padded.
	 * The number of written bytes to the buffer is fixedLength * 2 + 2. The additional two bytes are the terminating (zero) char which the client
	 * always requires. One could actually populate that last char normally and it would be displayed on client side, but then following data may get
	 * corrupted.
	 */
	protected final void writeS(String text, int fixedLength) {
		if (text == null || text.isEmpty()) {
			buf.writeZero(byteLengthForFixedString(fixedLength));
		} else {
			for (int i = 0; i < fixedLength; i++)
				buf.writeShortLE(i < text.length() ? text.charAt(i) : 0);
			buf.writeShortLE(0);
		}
	}

	/**
	 * Writes dye information (dye status + 3 byte RGB value) to the buffer.
	 * 
	 * @param rgb
	 *          - may be null
	 */
	protected final void writeDyeInfo(Integer rgb) {
		if (rgb == null) {
			writeB(new byte[4]);
		} else {
			writeC(1); // dye status (1 = dyed, 0 = not dyed)
			writeC((rgb & 0xFF0000) >> 16); // r
			writeC((rgb & 0xFF00) >> 8); // g
			writeC(rgb & 0xFF); // b
		}
	}

	@Override
	protected int getOpCodeZeroPadding() {
		return 3;
	}
}
