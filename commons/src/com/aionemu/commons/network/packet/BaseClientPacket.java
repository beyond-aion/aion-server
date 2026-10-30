package com.aionemu.commons.network.packet;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.NetworkUtils;

import io.netty.buffer.ByteBuf;

/**
 * Base class for every Client Packet
 *
 * @author -Nemesiss-
 * @param <T>
 *          connection - owner of this client packet.
 */
public abstract class BaseClientPacket<T> extends BasePacket implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(BaseClientPacket.class);
	private static final Set<Integer> partiallyReadPackets = ConcurrentHashMap.newKeySet();
	private static final Set<Integer> tooShortPackets = ConcurrentHashMap.newKeySet();
	/**
	 * Owner of this packet.
	 */
	private T client;
	/**
	 * Buffer that contains this packet data. The packet lets go of it after {@link #read()}, because the network layer releases it.
	 */
	private ByteBuf buf;

	/**
	 * Constructs a new client packet with specified id. The buffer must be later set with setBuffer method.
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
	public BaseClientPacket(ByteBuf buf, int opcode) {
		super(opcode);
		this.buf = buf;
	}

	/**
	 * Attach the data buffer to this packet.
	 *
	 * @param buf
	 */
	public void setBuffer(ByteBuf buf) {
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
	 * This method reads data from a packet buffer. All data must be read in {@link #readImpl()}, the buffer is not available afterwards.
	 *
	 * @return <code>true</code> if reading was successful, otherwise <code>false</code>
	 */
	public final boolean read() {
		int startPos = buf.readerIndex();
		try {
			readImpl();

			if (getRemainingBytes() > 0 && partiallyReadPackets.add(getOpCode()))
				log.warn("{} was not fully read! Last {} bytes were not read from buffer:\n{}", this, getRemainingBytes(),
					NetworkUtils.toHex(buf, startPos, buf.writerIndex()));

			return true;
		} catch (Exception ex) {
			if (ex.getClass() == IndexOutOfBoundsException.class) { // the buffer throws exactly this type when reading past the end of the packet
				if (tooShortPackets.add(getOpCode()))
					log.warn("{} from {} is shorter than expected:\n{}", this, client, NetworkUtils.toHex(buf, startPos, buf.writerIndex()));
				return false;
			}
			String msg = "Reading failed for packet " + this + ". Buffer Info";
			if (getRemainingBytes() > 0)
				msg += " (last " + getRemainingBytes() + " bytes were not read)";
			msg += ":\n" + NetworkUtils.toHex(buf, startPos, buf.writerIndex());
			log.error(msg, ex);
			return false;
		} finally {
			buf = null;
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
		return buf.readableBytes();
	}

	/**
	 * Read int from this packet buffer.
	 *
	 * @return int
	 */
	protected final int readD() {
		return buf.readIntLE();
	}

	/**
	 * Read byte from this packet buffer.
	 *
	 * @return byte
	 */
	protected final byte readC() {
		return buf.readByte();
	}

	/**
	 * Read unsigned byte from this packet buffer.
	 *
	 * @return int
	 */
	protected final int readUC() {
		return buf.readUnsignedByte();
	}

	/**
	 * Read short from this packet buffer.
	 *
	 * @return short
	 */
	protected final short readH() {
		return buf.readShortLE();
	}

	/**
	 * Read unsigned short from this packet buffer.
	 *
	 * @return int
	 */
	protected final int readUH() {
		return buf.readUnsignedShortLE();
	}

	/**
	 * Read double from this packet buffer.
	 *
	 * @return double
	 */
	protected final double readDF() {
		return buf.readDoubleLE();
	}

	/**
	 * Read double from this packet buffer.
	 *
	 * @return double
	 */
	protected final float readF() {
		return buf.readFloatLE();
	}

	/**
	 * Read long from this packet buffer.
	 *
	 * @return long
	 */
	protected final long readQ() {
		return buf.readLongLE();
	}

	/**
	 * Read String from this packet buffer.
	 *
	 * @return String
	 */
	protected final String readS() {
		StringBuilder sb = new StringBuilder();
		char ch;
		while ((ch = (char) buf.readShortLE()) != 0)
			sb.append(ch);
		return sb.toString();
	}

	/**
	 * Read n bytes from this packet buffer, n = length.
	 *
	 * @param length
	 * @return byte[]
	 */
	protected final byte[] readB(int length) {
		if (length < 0 || length > buf.readableBytes())
			throw new IndexOutOfBoundsException("Can't read " + length + " bytes, only " + buf.readableBytes() + " are left");
		byte[] result = new byte[length];
		buf.readBytes(result);
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
