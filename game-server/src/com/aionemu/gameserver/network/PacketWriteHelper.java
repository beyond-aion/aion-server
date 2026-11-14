package com.aionemu.gameserver.network;

import com.aionemu.gameserver.network.aion.AionServerPacket;

import io.netty.buffer.ByteBuf;

/**
 * @author -Nemesiss-
 */
public abstract class PacketWriteHelper {

	protected abstract void writeMe(ByteBuf buf);

	/**
	 * Write int to buffer.
	 *
	 * @param buf
	 * @param value
	 */
	protected static void writeD(ByteBuf buf, int value) {
		buf.writeIntLE(value);
	}

	/**
	 * Write short to buffer.
	 *
	 * @param buf
	 * @param value
	 */
	protected static void writeH(ByteBuf buf, int value) {
		buf.writeShortLE(value);
	}

	/**
	 * Write byte to buffer.
	 *
	 * @param buf
	 * @param value
	 */
	protected static void writeC(ByteBuf buf, int value) {
		buf.writeByte(value);
	}

	/**
	 * Write double to buffer.
	 *
	 * @param buf
	 * @param value
	 */
	protected static void writeDF(ByteBuf buf, double value) {
		buf.writeDoubleLE(value);
	}

	/**
	 * Write float to buffer.
	 *
	 * @param buf
	 * @param value
	 */
	protected static void writeF(ByteBuf buf, float value) {
		buf.writeFloatLE(value);
	}

	/**
	 * Write long to buffer.
	 *
	 * @param buf
	 * @param value
	 */
	protected static void writeQ(ByteBuf buf, long value) {
		buf.writeLongLE(value);
	}

	/**
	 * Write String to buffer
	 *
	 * @param buf
	 * @param text
	 */
	protected static void writeS(ByteBuf buf, String text) {
		if (text != null) {
			int len = text.length();
			for (int i = 0; i < len; i++)
				buf.writeShortLE(text.charAt(i));
		}
		buf.writeShortLE(0);
	}

	/**
	 * Write String to buffer
	 *
	 * @param buf
	 * @param text
	 * @param size
	 */
	protected static void writeS(ByteBuf buf, String text, int size) {
		if (text == null) {
			buf.writeZero(size);
		} else {
			int len = text.length();
			for (int i = 0; i < len; i++)
				buf.writeShortLE(text.charAt(i));
			buf.writeZero(size - (len * 2));
		}
	}

	/**
	 * Write byte array to buffer.
	 *
	 * @param buf
	 * @param data
	 */
	protected static void writeB(ByteBuf buf, byte[] data) {
		buf.writeBytes(data);
	}

	/**
	 * Skip specified amount of bytes
	 *
	 * @param buf
	 * @param bytes
	 */
	protected static void skip(ByteBuf buf, int bytes) {
		buf.writeZero(bytes);
	}

	/**
	 * @see AionServerPacket#writeDyeInfo(Integer rgb)
	 */
	protected static void writeDyeInfo(ByteBuf buf, Integer rgb) {
		if (rgb == null) {
			skip(buf, 4);
		} else {
			writeC(buf, 1); // dye status (1 = dyed, 0 = not dyed)
			writeC(buf, (rgb & 0xFF0000) >> 16); // r
			writeC(buf, (rgb & 0xFF00) >> 8); // g
			writeC(buf, rgb & 0xFF); // b
		}
	}
}
