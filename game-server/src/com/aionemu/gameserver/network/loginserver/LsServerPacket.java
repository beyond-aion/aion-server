package com.aionemu.gameserver.network.loginserver;

import com.aionemu.commons.network.packet.BaseServerPacket;

import io.netty.buffer.ByteBuf;

/**
 * Base class for every GameServer -> Login Server Packet.
 * 
 * @author -Nemesiss-
 */
public abstract class LsServerPacket extends BaseServerPacket {

	/**
	 * constructs new server packet with specified opcode.
	 * 
	 * @param opcode
	 *          packet id
	 */
	protected LsServerPacket(int opcode) {
		super(opcode);
	}

	/**
	 * Write this packet data for given connection to the end of the buffer.
	 * 
	 * @param con
	 * @param buf
	 */
	public final void write(LoginServerConnection con, ByteBuf buffer) {
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
	protected abstract void writeImpl(LoginServerConnection con);
}
