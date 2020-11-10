package com.aionemu.chatserver.network.gameserver;

import java.nio.ByteBuffer;

import com.aionemu.commons.network.packet.BaseServerPacket;

/**
 * Base class for every LS -> GameServer Server Packet.
 * 
 * @author -Nemesiss-
 */
public abstract class GsServerPacket extends BaseServerPacket {

	protected GsServerPacket() {
		super(0);
	}

	/**
	 * Write this packet data for given connection, to given buffer.
	 */
	public final void write(GsConnection con, ByteBuffer buffer) {
		setBuf(buffer);
		buf.putShort((short) 0);
		writeImpl(con);
		buf.flip();
		buf.putShort((short) buf.limit());
		buf.position(0);
	}

	/**
	 * Write data that this packet represents to given byte buffer.
	 */
	protected abstract void writeImpl(GsConnection con);
}
