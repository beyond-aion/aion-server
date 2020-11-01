package com.aionemu.chatserver.network.aion;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aionemu.chatserver.common.netty.BaseServerPacket;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;

/**
 * @author ATracer
 */
public abstract class AbstractServerPacket extends BaseServerPacket {

	public AbstractServerPacket(byte opCode) {
		super(opCode);
	}

	public void write(ClientChannelHandler clientChannelHandler, ChannelBuffer buf) {
		buf.writeShort((short) 0);
		writeImpl(clientChannelHandler, buf);
	}

	protected abstract void writeImpl(ClientChannelHandler cHandler, ChannelBuffer buf);
}
