package com.aionemu.chatserver.network.netty;

import java.nio.ByteOrder;

import io.netty.channel.Channel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class NettyPipeline {

	/**
	 * Sets up the channel for packets with a little-endian size header of two bytes, which counts itself.
	 */
	public static void init(Channel channel, NettyConnection<?> connection, int maxPacketSize) {
		channel.pipeline()
			.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, maxPacketSize, 0, 2, -2, 2, true))
			.addLast("packetEncoder", new PacketEncoder(connection, maxPacketSize))
			.addLast("connectionHandler", new ConnectionHandler(connection));
	}
}
