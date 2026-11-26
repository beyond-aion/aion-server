package com.aionemu.loginserver.network.aion;

import com.aionemu.commons.network.packet.BaseServerPacket;

import io.netty.buffer.ByteBuf;

/**
 * Base class for every LS -> Aion Server Packet.
 * 
 * @author -Nemesiss-
 */
public abstract class AionServerPacket extends BaseServerPacket {

	/**
	 * Constructs a new server packet with specified id.
	 * 
	 * @param opcode
	 *          packet opcode.
	 */
	protected AionServerPacket(int opcode) {
		super(opcode);
	}

	/**
	 * Write this packet data for given connection to the end of the buffer. The encryption happens afterwards, in the channel pipeline.
	 * 
	 * @param con
	 * @param buffer
	 */
	public final void write(LoginConnection con, ByteBuf buffer) {
		setBuf(buffer);
		int start = buf.writerIndex();
		buf.writeShortLE(0);
		buf.writeByte(getOpCode());
		writeImpl(con);
		buf.setShortLE(start, buf.writerIndex() - start);
	}

	/**
	 * Write data that this packet represents to given byte buffer.
	 * 
	 * @param con
	 * @param buf
	 */
	protected abstract void writeImpl(LoginConnection con);
}
