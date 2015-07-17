package com.aionemu.chatserver.network.netty.coder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 * @author ATracer
 */
public class GameServerPacketEncoder extends OneToOneEncoder
{
	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel arg1, Object arg2) throws Exception
	{
		ChannelBuffer message = (ChannelBuffer) arg2;
		message.setShort(0, (short) message.readableBytes());
		return message;
	}
}
