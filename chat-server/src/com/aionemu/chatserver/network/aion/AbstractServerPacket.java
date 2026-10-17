package com.aionemu.chatserver.network.aion;

import com.aionemu.commons.network.packet.BaseServerPacket;

import io.netty.buffer.ByteBuf;

/**
 * @author ATracer
 */
public abstract class AbstractServerPacket extends BaseServerPacket {

	public AbstractServerPacket(int opCode) {
		super(opCode);
	}

	public final void write(AionConnection connection, ByteBuf buffer) {
		setBuf(buffer);
		int start = buf.writerIndex();
		buf.writeShortLE(0);
		writeImpl(connection);
		buf.setShortLE(start, buf.writerIndex() - start);
	}

	protected abstract void writeImpl(AionConnection connection);
}
