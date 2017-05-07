package com.aionemu.gameserver.network.loginserver;

import java.nio.ByteBuffer;

import com.aionemu.commons.network.packet.BaseServerPacket;

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
	 * Write this packet data for given connection, to given buffer.
	 * 
	 * @param con
	 * @param buf
	 */
	public final void write(LoginServerConnection con, ByteBuffer buffer) {
		setBuf(buffer);
		buf.putShort((short) 0);
		buf.put((byte) this.getOpCode());
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
	protected abstract void writeImpl(LoginServerConnection con);
}
