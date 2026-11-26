package com.aionemu.loginserver.network.gameserver;

import com.aionemu.commons.network.packet.BaseServerPacket;

import io.netty.buffer.ByteBuf;

/**
 * Base class for every LS -> GameServer Server Packet.
 * 
 * @author -Nemesiss-
 */
public abstract class GsServerPacket extends BaseServerPacket {

	/**
	 * Constructs a new server packet with specified id.
	 * 
	 * @param opcode
	 *          packet opcode.
	 */
	protected GsServerPacket() {
		super(0);
	}

	/**
	 * Write this packet data for given connection to the end of the buffer.
	 * 
	 * @param con
	 * @param buf
	 */
	public final void write(GsConnection con, ByteBuf buffer) {
		setBuf(buffer);
		int start = buf.writerIndex();
		buf.writeShortLE(0);
		writeImpl(con);
		buf.setShortLE(start, buf.writerIndex() - start);
	}

	/**
	 * Write data that this packet represents to given byte buffer.
	 * 
	 * @param con
	 * @param buf
	 */
	protected abstract void writeImpl(GsConnection con);
}
