package com.aionemu.chatserver.network.netty;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.aionemu.commons.network.packet.BaseServerPacket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Lets the connection write a server packet, including its size header, into the outgoing buffer.
 */
class PacketEncoder extends MessageToByteEncoder<BaseServerPacket> {

	private final NettyConnection<?> connection;
	private final int maxPacketSize;

	PacketEncoder(NettyConnection<?> connection, int maxPacketSize) {
		super(BaseServerPacket.class);
		this.connection = connection;
		this.maxPacketSize = maxPacketSize;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, BaseServerPacket packet, ByteBuf out) {
		out.ensureWritable(maxPacketSize);
		ByteBuffer buffer = out.nioBuffer(out.writerIndex(), maxPacketSize).order(ByteOrder.LITTLE_ENDIAN);
		connection.encode(packet, buffer);
		out.writerIndex(out.writerIndex() + buffer.limit());
	}
}
