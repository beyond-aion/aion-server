package com.aionemu.commons.network.netty;

import java.nio.ByteOrder;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.AttributeKey;

public class NettyPipeline {

	private static final AttributeKey<NettyConnection<?>> CONNECTION = AttributeKey.valueOf(NettyConnection.class, "connection");

	/**
	 * Sets up the channel for packets with a little-endian size header of two bytes, which counts itself.
	 */
	public static void init(Channel channel, NettyConnection<?> connection, int maxPacketSize) {
		init(channel, connection, maxPacketSize, maxPacketSize, null);
	}

	/**
	 * Sets up the channel for packets with a little-endian size header of two bytes, which counts itself.
	 *
	 * @param maxReceivedPacketSize
	 *          Larger packets close the connection.
	 * @param maxSentPacketSize
	 *          Larger packets are logged and dropped.
	 * @param cryptHandler
	 *          Handler that decrypts received packets (without their size header) and encrypts sent packets (with it, so it can update the size), or
	 *          null if the data is not encrypted.
	 */
	public static void init(Channel channel, NettyConnection<?> connection, int maxReceivedPacketSize, int maxSentPacketSize,
		ChannelHandler cryptHandler) {
		channel.attr(CONNECTION).set(connection);
		ChannelPipeline pipeline = channel.pipeline();
		pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, maxReceivedPacketSize, 0, 2, -2, 2, true));
		if (cryptHandler != null)
			pipeline.addLast("crypt", cryptHandler);
		pipeline.addLast("packetEncoder", new PacketEncoder(connection, maxSentPacketSize));
		pipeline.addLast("connectionHandler", new ConnectionHandler(connection));
	}

	static NettyConnection<?> getConnection(Channel channel) {
		return channel.attr(CONNECTION).get();
	}
}
