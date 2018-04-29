package com.aionemu.gameserver.network;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author -Nemesiss-
 */
public abstract class PacketWriteHelper {

	protected abstract void writeMe(ByteBuffer buf);

	/**
	 * Write int to buffer.
	 * 
	 * @param buf
	 * @param value
	 */
	protected static void writeD(ByteBuffer buf, int value) {
		buf.putInt(value);
	}

	/**
	 * Write short to buffer.
	 * 
	 * @param buf
	 * @param value
	 */
	protected static void writeH(ByteBuffer buf, int value) {
		buf.putShort((short) value);
	}

	/**
	 * Write byte to buffer.
	 * 
	 * @param buf
	 * @param value
	 */
	protected static void writeC(ByteBuffer buf, int value) {
		buf.put((byte) value);
	}

	/**
	 * Write double to buffer.
	 * 
	 * @param buf
	 * @param value
	 */
	protected static void writeDF(ByteBuffer buf, double value) {
		buf.putDouble(value);
	}

	/**
	 * Write float to buffer.
	 * 
	 * @param buf
	 * @param value
	 */
	protected static void writeF(ByteBuffer buf, float value) {
		buf.putFloat(value);
	}

	/**
	 * Write long to buffer.
	 * 
	 * @param buf
	 * @param value
	 */
	protected static void writeQ(ByteBuffer buf, long value) {
		buf.putLong(value);
	}

	/**
	 * Write String to buffer
	 * 
	 * @param buf
	 * @param text
	 */
	protected static void writeS(ByteBuffer buf, String text) {
		if (text == null) {
			buf.putChar('\000');
		} else {
			int len = text.length();
			for (int i = 0; i < len; i++)
				buf.putChar(text.charAt(i));
			buf.putChar('\000');
		}
	}

	/**
	 * Write String to buffer
	 * 
	 * @param buf
	 * @param text
	 * @param size
	 */
	protected static void writeS(ByteBuffer buf, String text, int size) {
		if (text == null) {
			buf.put(new byte[size]);
		} else {
			int len = text.length();
			for (int i = 0; i < len; i++)
				buf.putChar(text.charAt(i));
			buf.put(new byte[size - (len * 2)]);
		}
	}

	/**
	 * Write byte array to buffer.
	 * 
	 * @param buf
	 * @param data
	 */
	protected static void writeB(ByteBuffer buf, byte[] data) {
		buf.put(data);
	}

	/**
	 * Skip specified amount of bytes
	 * 
	 * @param buf
	 * @param bytes
	 */
	protected static void skip(ByteBuffer buf, int bytes) {
		buf.put(new byte[bytes]);
	}

	/**
	 * @see AionServerPacket#writeDyeInfo(Integer rgb)
	 */
	protected static void writeDyeInfo(ByteBuffer buf, Integer rgb) {
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
