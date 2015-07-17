package com.aionemu.chatserver.network.netty.coder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

/**
 * @author ATracer
 */
public class LoginPacketDecoder extends OneToOneDecoder
{
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel arg1, Object arg2) throws Exception
	{
		ChannelBuffer message = (ChannelBuffer) arg2;
		return message;
	}
}
