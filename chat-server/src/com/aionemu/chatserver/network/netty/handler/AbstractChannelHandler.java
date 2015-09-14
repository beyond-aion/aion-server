package com.aionemu.chatserver.network.netty.handler;

import java.io.IOException;
import java.net.InetAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.common.netty.BaseServerPacket;

/**
 * @author ATracer
 */
public abstract class AbstractChannelHandler extends SimpleChannelUpstreamHandler
{
	private static final Logger log = LoggerFactory.getLogger(AbstractChannelHandler.class);

	/**
	 * IP address of channel client
	 */
	protected InetAddress inetAddress;
	/**
	 * Associated channel
	 */
	protected Channel channel;

	/**
	 * Invoked when a Channel was disconnected from its remote peer
	 */
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
	{
		log.info("Channel disconnected IP: " + inetAddress.getHostAddress());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
	{
		if (!(e.getCause() instanceof IOException))
		{
			log.error("NETTY: Exception caught: ", e.getCause());
		}
	}

	/**
	 * Closes the channel but ensures that packet is send before close
	 * 
	 * @param packet
	 */
	public void close(BaseServerPacket packet)
	{
		channel.write(packet).addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * Closes the channel
	 */
	public void close()
	{
		channel.close();
	}

	/**
	 * @return the IP address string
	 */
	public String getIP()
	{
		return inetAddress.getHostAddress();
	}
}
