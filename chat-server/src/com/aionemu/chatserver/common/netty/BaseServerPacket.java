package com.aionemu.chatserver.common.netty;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @author ATracer
 */
public abstract class BaseServerPacket extends AbstractPacket {

	public BaseServerPacket(byte opCode) {
		super(opCode);
	}

	/**
	 * Write int to buffer
	 */
	protected final void writeD(ChannelBuffer buf, int value) {
		buf.writeInt(value);
	}

	/**
	 * Write short to buffer
	 */
	protected final void writeH(ChannelBuffer buf, int value) {
		buf.writeShort((short) value);
	}

	/**
	 * Write byte to Buffer
	 */
	protected final void writeC(ChannelBuffer buf, int value) {
		buf.writeByte((byte) value);
	}

	/**
	 * Write double to buffer
	 */
	protected final void writeDF(ChannelBuffer buf, double value) {
		buf.writeDouble(value);
	}

	/**
	 * Write float to buffer
	 */
	protected final void writeF(ChannelBuffer buf, float value) {
		buf.writeFloat(value);
	}

	/**
	 * Write byte array to buffer
	 */
	protected final void writeB(ChannelBuffer buf, byte[] data) {
		buf.writeBytes(data);
	}

	/**
	 * Write String to buffer
	 */
	protected final void writeS(ChannelBuffer buf, String text) {
		if (text != null) {
			final int len = text.length();
			for (int i = 0; i < len; i++)
				buf.writeChar(text.charAt(i));
		}
		buf.writeChar('\000');
	}

	/**
	 * Write long to buffer
	 */
	protected final void writeQ(ChannelBuffer buf, long data) {
		buf.writeLong(data);
	}
}
