package com.aionemu.chatserver.network.aion;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aionemu.chatserver.common.netty.BaseClientPacket;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;

/**
 * @author ATracer
 */
public abstract class AbstractClientPacket extends BaseClientPacket {

	protected final ClientChannelHandler clientChannelHandler;

	public AbstractClientPacket(ChannelBuffer channelBuffer, ClientChannelHandler clientChannelHandler, byte opCode) {
		super(channelBuffer, opCode);
		this.clientChannelHandler = clientChannelHandler;
	}
}
