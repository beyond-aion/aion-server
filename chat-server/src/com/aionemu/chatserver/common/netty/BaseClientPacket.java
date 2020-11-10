package com.aionemu.chatserver.common.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.NetworkUtils;

public abstract class BaseClientPacket extends AbstractPacket {

	private static final Logger log = LoggerFactory.getLogger(BaseClientPacket.class);

	private final ChannelBuffer buf;

	public BaseClientPacket(ChannelBuffer channelBuffer, byte opCode) {
		super(opCode);
		this.buf = channelBuffer;
	}

	public int getRemainingBytes() {
		return buf.readableBytes();
	}

	/**
	 * Perform packet read
	 */
	public boolean read() {
		int startPos = buf.readerIndex();
		try {
			readImpl();
			if (getRemainingBytes() > 0)
				log.warn("{} was not fully read! Last {} bytes were not read from buffer: \n{}", this, getRemainingBytes(),
					NetworkUtils.toHex(buf.toByteBuffer(startPos, buf.writerIndex() - startPos)));
			return true;
		} catch (Exception ex) {
			String msg = "Reading failed for packet " + this + ". Buffer Info";
			if (getRemainingBytes() > 0)
				msg += " (last " + getRemainingBytes() + " bytes were not read)";
			msg += ":\n" + NetworkUtils.toHex(buf.toByteBuffer(startPos, buf.writerIndex() - startPos));
			log.error(msg, ex);
			return false;
		}
	}

	/**
	 * Perform packet action
	 */
	public void run() {
		try {
			runImpl();
		} catch (Exception ex) {
			log.error("Running failed for packet {}", this, ex);
		}
	}

	protected abstract void readImpl();

	protected abstract void runImpl();

	/**
	 * Read int from this packet buffer.
	 */
	protected final int readD() {
		try {
			return buf.readInt();
		} catch (Exception e) {
			log.error("Missing D for: {}", this);
		}
		return 0;
	}

	/**
	 * Read byte from this packet buffer.
	 */
	protected final int readC() {
		try {
			return buf.readByte() & 0xFF;
		} catch (Exception e) {
			log.error("Missing C for: {}", this);
		}
		return 0;
	}

	/**
	 * Read short from this packet buffer.
	 */
	protected final int readH() {
		try {
			return buf.readShort() & 0xFFFF;
		} catch (Exception e) {
			log.error("Missing H for: {}", this);
		}
		return 0;
	}

	/**
	 * Read double from this packet buffer.
	 */
	protected final double readDF() {
		try {
			return buf.readDouble();
		} catch (Exception e) {
			log.error("Missing DF for: {}", this);
		}
		return 0;
	}

	/**
	 * Read double from this packet buffer.
	 */
	protected final float readF() {
		try {
			return buf.readFloat();
		} catch (Exception e) {
			log.error("Missing F for: {}", this);
		}
		return 0;
	}

	/**
	 * Read long from this packet buffer.
	 */
	protected final long readQ() {
		try {
			return buf.readLong();
		} catch (Exception e) {
			log.error("Missing Q for: {}", this);
		}
		return 0;
	}

	/**
	 * Read String from this packet buffer.
	 */
	protected final String readS() {
		StringBuilder sb = new StringBuilder();
		char ch;
		try {
			while ((ch = buf.readChar()) != 0)
				sb.append(ch);
		} catch (Exception e) {
			log.error("Missing S for: {}", this);
		}
		return sb.toString();
	}

	/**
	 * Read n bytes from this packet buffer, n = length.
	 */
	protected final byte[] readB(int length) {
		byte[] result = new byte[length];
		try {
			buf.readBytes(result);
		} catch (Exception e) {
			log.error("Missing byte[] for: {}", this);
		}
		return result;
	}
}
