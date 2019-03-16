package com.aionemu.commons.network.packet;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.AConnection;
import com.aionemu.commons.utils.NetworkUtils;

/**
 * Base class for every Client Packet
 * 
 * @author -Nemesiss-
 * @param <T>
 *          AConnection - owner of this client packet.
 */
public abstract class BaseClientPacket<T extends AConnection<?>> extends BasePacket implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(BaseClientPacket.class);
	private static final Set<Integer> partiallyReadPackets = ConcurrentHashMap.newKeySet();
	/**
	 * Owner of this packet.
	 */
	private T client;
	/**
	 * ByteBuffer that contains this packet data
	 */
	private ByteBuffer buf;

	/**
	 * Constructs a new client packet with specified id. ByteBuffer must be later set with setBuffer method.
	 * 
	 * @param opcode
	 *          packet opcode.
	 */
	public BaseClientPacket(int opcode) {
		this(null, opcode);
	}

	/**
	 * Constructs a new client packet with specified id and data buffer.
	 * 
	 * @param buf
	 *          packet data container.
	 * @param opcode
	 *          packet opcode.
	 */
	public BaseClientPacket(ByteBuffer buf, int opcode) {
		super(opcode);
		this.buf = buf;
	}

	/**
	 * Attach ByteBuffer to this packet.
	 * 
	 * @param buf
	 */
	public void setBuffer(ByteBuffer buf) {
		this.buf = buf;
	}

	/**
	 * Attach client connection to this packet.
	 * 
	 * @param client
	 */
	public void setConnection(T client) {
		this.client = client;
	}

	/**
	 * This method reads data from a packet buffer. If the error occurred while reading data, the connection is closed.
	 * 
	 * @return <code>true</code> if reading was successful, otherwise <code>false</code>
	 */
	public final boolean read() {
		int startPos = buf.position();
		try {
			readImpl();

			if (getRemainingBytes() > 0 && partiallyReadPackets.add(getOpCode()))
				log.warn(this + " was not fully read! Last " + getRemainingBytes() + " bytes were not read from buffer:\n" + NetworkUtils.toHex(buf, startPos, buf.limit()));

			return true;
		} catch (Exception ex) {
			String msg = "Reading failed for packet " + this + ". Buffer Info";
			if (getRemainingBytes() > 0)
				msg += " (last " + getRemainingBytes() + " bytes were not read)";
			msg += ":\n" + NetworkUtils.toHex(buf, startPos, buf.limit());
			log.error(msg, ex);
			return false;
		}
	}

	/**
	 * Data reading implementation
	 */
	protected abstract void readImpl();

	/**
	 * @return number of bytes remaining in this packet buffer.
	 */
	public final int getRemainingBytes() {
		return buf.remaining();
	}

	/**
	 * Read int from this packet buffer.
	 * 
	 * @return int
	 */
	protected final int readD() {
		try {
			return buf.getInt();
		} catch (BufferUnderflowException e) {
			log.error("Missing D for: " + this + " (sent from " + client + ")");
		}
		return 0;
	}

	/**
	 * Read byte from this packet buffer.
	 * 
	 * @return byte
	 */
	protected final byte readC() {
		try {
			return buf.get();
		} catch (BufferUnderflowException e) {
			log.error("Missing C for: " + this + " (sent from " + client + ")");
		}
		return 0;
	}

	/**
	 * Read unsigned byte from this packet buffer.
	 * 
	 * @return int
	 */
	protected final int readUC() {
		try {
			return buf.get() & 0xFF;
		} catch (BufferUnderflowException e) {
			log.error("Missing C for: " + this + " (sent from " + client + ")");
		}
		return 0;
	}

	/**
	 * Read short from this packet buffer.
	 * 
	 * @return short
	 */
	protected final short readH() {
		try {
			return buf.getShort();
		} catch (BufferUnderflowException e) {
			log.error("Missing H for: " + this + " (sent from " + client + ")");
		}
		return 0;
	}

	/**
	 * Read unsigned short from this packet buffer.
	 * 
	 * @return int
	 */
	protected final int readUH() {
		try {
			return buf.getShort() & 0xFFFF;
		} catch (BufferUnderflowException e) {
			log.error("Missing H for: " + this + " (sent from " + client + ")");
		}
		return 0;
	}

	/**
	 * Read double from this packet buffer.
	 * 
	 * @return double
	 */
	protected final double readDF() {
		try {
			return buf.getDouble();
		} catch (BufferUnderflowException e) {
			log.error("Missing DF for: " + this + " (sent from " + client + ")");
		}
		return 0;
	}

	/**
	 * Read double from this packet buffer.
	 * 
	 * @return double
	 */
	protected final float readF() {
		try {
			return buf.getFloat();
		} catch (BufferUnderflowException e) {
			log.error("Missing F for: " + this + " (sent from " + client + ")");
		}
		return 0;
	}

	/**
	 * Read long from this packet buffer.
	 * 
	 * @return long
	 */
	protected final long readQ() {
		try {
			return buf.getLong();
		} catch (BufferUnderflowException e) {
			log.error("Missing Q for: " + this + " (sent from " + client + ")");
		}
		return 0;
	}

	/**
	 * Read String from this packet buffer.
	 * 
	 * @return String
	 */
	protected final String readS() {
		StringBuilder sb = new StringBuilder();
		char ch;
		try {
			while ((ch = buf.getChar()) != 0)
				sb.append(ch);
		} catch (BufferUnderflowException e) {
			log.error("Missing S for: " + this + " (sent from " + client + ")");
		}
		return sb.toString();
	}

	/**
	 * Read n bytes from this packet buffer, n = length.
	 * 
	 * @param length
	 * @return byte[]
	 */
	protected final byte[] readB(int length) {
		byte[] result = new byte[length];
		try {
			buf.get(result);
		} catch (BufferUnderflowException e) {
			log.error("Missing byte[] for: " + this + " (sent from " + client + ")");
		}
		return result;
	}

	/**
	 * Execute this packet action.
	 */
	protected abstract void runImpl();

	/**
	 * @return Connection that is owner of this packet.
	 */
	public final T getConnection() {
		return client;
	}
}
