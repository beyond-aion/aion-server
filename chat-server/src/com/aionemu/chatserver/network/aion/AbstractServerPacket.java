package com.aionemu.chatserver.network.aion;

import java.nio.ByteBuffer;

import com.aionemu.commons.network.packet.BaseServerPacket;

/**
 * @author ATracer
 */
public abstract class AbstractServerPacket extends BaseServerPacket {

	public AbstractServerPacket(int opCode) {
		super(opCode);
	}

	public final void write(AionConnection connection, ByteBuffer buffer) {
		setBuf(buffer);
		buf.putShort((short) 0);
		writeImpl(connection);
		buf.flip();
		buf.putShort((short) buf.limit());
		buf.position(0);
	}

	protected abstract void writeImpl(AionConnection connection);
}
