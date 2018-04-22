package com.aionemu.gameserver.network.aion;

import java.nio.ByteBuffer;

import com.aionemu.commons.network.packet.BaseServerPacket;
import com.aionemu.gameserver.network.Crypt;

/**
 * Base class for every GS -> Aion Server Packet.
 * 
 * @author -Nemesiss-
 */
public abstract class AionServerPacket extends BaseServerPacket {

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
	 * 
	 * @param buf
	 * @param value
	 */
	private final void writeOP() {
		/** obfuscate packet id */
		int op = Crypt.encodeOpcodec(getOpCode());
		buf.putShort((short) (op));
		/** put static server packet code */
		buf.put(Crypt.staticServerPacketCode);

		/** for checksum? */
		buf.putShort((short) (~op));
	}

	/**
	 * Write and encrypt this packet data for given connection, to given buffer.
	 * 
	 * @param con
	 * @param buf
	 */
	public final void write(AionConnection con, ByteBuffer buffer) {
		setBuf(buffer);
		buf.putShort((short) 0);
		writeOP();
		writeImpl(con);
		buf.flip();
		buf.putShort((short) buf.limit());
		ByteBuffer b = buf.slice();
		buf.position(0);
		con.encrypt(b);
	}

	/**
	 * Write data that this packet represents to given byte buffer.
	 * 
	 * @param con
	 * @param buf
	 */
	protected void writeImpl(AionConnection con) {

	}

	public final ByteBuffer getBuf() {
		return this.buf;
	}

	/**
	 * Write String to buffer
	 * 
	 * @param text
	 * @param size
	 */
	protected final void writeS(String text, int size) {
		if (text == null) {
			buf.put(new byte[size]);
		} else {
			final int len = text.length();
			for (int i = 0; i < len; i++)
				buf.putChar(text.charAt(i));
			buf.put(new byte[size - (len * 2)]);
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
	public String toFormattedPacketNameString() {
		return String.format("[0x%03X] %s", getOpCode(), getPacketName());
	}
}
