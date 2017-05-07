package com.aionemu.commons.network.packet;

import java.nio.ByteBuffer;

/**
 * Base class for every Server Packet
 * 
 * @author -Nemesiss-
 */
public abstract class BaseServerPacket extends BasePacket {

	/**
	 * ByteBuffer that contains this packet data
	 */
	public ByteBuffer buf;

	/**
	 * Constructs a new server packet.<br>
	 * If this constructor was used, then {@link #setOpcode(int)} must be called
	 */
	protected BaseServerPacket() {
		super();
	}

	/**
	 * Constructs a new server packet with specified id.
	 * 
	 * @param opCode
	 *          packet opcode.
	 */
	protected BaseServerPacket(int opCode) {
		super(opCode);
	}

	/**
	 * @param buf
	 *          the buf to set
	 */
	public void setBuf(ByteBuffer buf) {
		this.buf = buf;
	}

	/**
	 * Write int to buffer.
	 * 
	 * @param buf
	 * @param value
	 */
	protected final void writeD(int value) {
		buf.putInt(value);
	}

	/**
	 * Write short to buffer.
	 * 
	 * @param buf
	 * @param value
	 */
	protected final void writeH(int value) {
		buf.putShort((short) value);
	}

	/**
	 * Write byte to buffer.
	 * 
	 * @param buf
	 * @param value
	 */
	protected final void writeC(int value) {
		buf.put((byte) value);
	}

	/**
	 * Write byte to buffer.
	 * 
	 * @param buf
	 * @param value
	 */
	protected final void writeC(byte value) {
		buf.put(value);
	}

	/**
	 * Write double to buffer.
	 * 
	 * @param buf
	 * @param value
	 */
	protected final void writeDF(double value) {
		buf.putDouble(value);
	}

	/**
	 * Write float to buffer.
	 * 
	 * @param buf
	 * @param value
	 */
	protected final void writeF(float value) {
		buf.putFloat(value);
	}

	/**
	 * Write long to buffer.
	 * 
	 * @param buf
	 * @param value
	 */
	protected final void writeQ(long value) {
		buf.putLong(value);
	}

	/**
	 * Write String to buffer
	 * 
	 * @param buf
	 * @param text
	 */
	protected final void writeS(String text) {
		if (text == null) {
			buf.putChar('\000');
		} else {
			final int len = text.length();
			for (int i = 0; i < len; i++)
				buf.putChar(text.charAt(i));
			buf.putChar('\000');
		}
	}

	/**
	 * Write byte array to buffer.
	 * 
	 * @param buf
	 * @param data
	 */
	protected final void writeB(byte[] data) {
		buf.put(data);
	}
}
