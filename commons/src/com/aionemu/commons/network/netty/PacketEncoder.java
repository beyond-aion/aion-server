package com.aionemu.commons.network.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.packet.BaseServerPacket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Lets the connection write a server packet, including its size header, into the outgoing buffer. A packet that can't be written or exceeds the
 * size limit is logged and dropped before it reaches the encryption, so the encryption state stays in sync with the remote side.
 */
class PacketEncoder extends MessageToByteEncoder<BaseServerPacket> {

	private static final Logger log = LoggerFactory.getLogger(PacketEncoder.class);

	private final NettyConnection<?> connection;
	private final int maxPacketSize;

	PacketEncoder(NettyConnection<?> connection, int maxPacketSize) {
		super(BaseServerPacket.class);
		this.connection = connection;
		this.maxPacketSize = maxPacketSize;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, BaseServerPacket packet, ByteBuf out) {
		int start = out.writerIndex();
		try {
			connection.encode(packet, out);
		} catch (Exception e) {
			log.error("Could not write {} for {}", packet, connection, e);
			out.writerIndex(start);
			return;
		}
		int size = out.writerIndex() - start;
		if (size > maxPacketSize) {
			log.warn("{} was not sent to {}, because its {} bytes exceed the limit of {} bytes", packet, connection, size, maxPacketSize);
			out.writerIndex(start);
		}
	}
}
