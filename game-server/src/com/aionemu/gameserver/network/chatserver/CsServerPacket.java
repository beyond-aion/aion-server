package com.aionemu.gameserver.network.chatserver;

import java.nio.ByteBuffer;

import com.aionemu.commons.network.packet.BaseServerPacket;

/**
 * @author ATracer
 */
public abstract class CsServerPacket extends BaseServerPacket {

	/**
	 * constructs new server packet with specified opcode.
	 * 
	 * @param opcode
	 *          packet id
	 */
	protected CsServerPacket(int opcode) {
		super(opcode);
	}

	/**
	 * Write this packet data for given connection, to given buffer.
	 * 
	 * @param con
	 * @param buf
	 */
	public final void write(ChatServerConnection con, ByteBuffer buffer) {
		setBuf(buffer);
		buf.putShort((short) 0);
		buf.put((byte) getOpCode());
		writeImpl(con);
		buf.flip();
		buf.putShort((short) buf.limit());
		buf.position(0);
	}

	/**
	 * Write data that this packet represents to given byte buffer.
	 * 
	 * @param con
	 * @param buf
	 */
	protected abstract void writeImpl(ChatServerConnection con);
}
