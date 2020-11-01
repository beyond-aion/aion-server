package com.aionemu.chatserver.network.netty.handler;

import java.io.IOException;
import java.net.InetAddress;

import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.common.netty.BaseServerPacket;

/**
 * @author ATracer
 */
public abstract class AbstractChannelHandler extends SimpleChannelUpstreamHandler {

	private static final Logger log = LoggerFactory.getLogger(AbstractChannelHandler.class);

	protected InetAddress inetAddress;

	protected Channel associatedChannel;

	/**
	 * Invoked when a Channel was disconnected from its remote peer
	 */
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		log.info("Channel disconnected IP: {}", inetAddress.getHostAddress());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		if (e.getCause() instanceof IOException)
			return;
		log.error("Caught exception from netty", e.getCause());
	}

	/**
	 * Closes the channel but ensures that packet is send before close
	 * 
	 * @param packet
	 *          Packet to be send before the channel is closed
	 */
	public void close(BaseServerPacket packet) {
		associatedChannel.write(packet).addListener(ChannelFutureListener.CLOSE);
	}

	public void close() {
		associatedChannel.close();
	}

	public String getIP() {
		return inetAddress.getHostAddress();
	}
}
