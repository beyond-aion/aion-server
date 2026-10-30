package com.aionemu.commons.network.packet;

import io.netty.buffer.ByteBuf;

/**
 * Base class for every Server Packet
 *
 * @author -Nemesiss-
 */
public abstract class BaseServerPacket extends BasePacket {

	/**
	 * Buffer this packet is being written to. It is only set while the packet is being written.
	 */
	protected ByteBuf buf;

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
	public void setBuf(ByteBuf buf) {
		this.buf = buf;
	}

	/**
	 * Write int to buffer.
	 *
	 * @param value
	 */
	protected final void writeD(int value) {
		buf.writeIntLE(value);
	}

	/**
	 * Write short to buffer.
	 *
	 * @param value
	 */
	protected final void writeH(int value) {
		buf.writeShortLE(value);
	}

	/**
	 * Write byte to buffer.
	 *
	 * @param value
	 */
	protected final void writeC(int value) {
		buf.writeByte(value);
	}

	/**
	 * Write byte to buffer.
	 *
	 * @param value
	 */
	protected final void writeC(byte value) {
		buf.writeByte(value);
	}

	/**
	 * Write double to buffer.
	 *
	 * @param value
	 */
	protected final void writeDF(double value) {
		buf.writeDoubleLE(value);
	}

	/**
	 * Write float to buffer.
	 *
	 * @param value
	 */
	protected final void writeF(float value) {
		buf.writeFloatLE(value);
	}

	/**
	 * Write long to buffer.
	 *
	 * @param value
	 */
	protected final void writeQ(long value) {
		buf.writeLongLE(value);
	}

	/**
	 * Write String to buffer
	 *
	 * @param text
	 */
	protected final void writeS(String text) {
		if (text != null) {
			final int len = text.length();
			for (int i = 0; i < len; i++)
				buf.writeShortLE(text.charAt(i));
		}
		buf.writeShortLE(0);
	}

	/**
	 * Write byte array to buffer.
	 *
	 * @param data
	 */
	protected final void writeB(byte[] data) {
		buf.writeBytes(data);
	}
}
