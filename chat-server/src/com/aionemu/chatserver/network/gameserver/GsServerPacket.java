package com.aionemu.chatserver.network.gameserver;

import com.aionemu.commons.network.packet.BaseServerPacket;

import io.netty.buffer.ByteBuf;

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
	public final void write(GsConnection con, ByteBuf buffer) {
		setBuf(buffer);
		int start = buf.writerIndex();
		buf.writeShortLE(0);
		writeImpl(con);
		buf.setShortLE(start, buf.writerIndex() - start);
	}

	/**
	 * Write data that this packet represents to given byte buffer.
	 */
	protected abstract void writeImpl(GsConnection con);
}
