package com.aionemu.commons.network.netty;

import java.nio.ByteOrder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Splits the received data into packets with a little-endian size header of two bytes, which counts itself. Once the channel is closed, the rest
 * of the data is discarded: Netty's decoder would otherwise keep passing on the packets that are left in the data read at once, so a client that
 * sends thousands of invalid packets in one go would make every one of them log a warning and close the channel again.
 */
class FrameDecoder extends LengthFieldBasedFrameDecoder {

	FrameDecoder(int maxPacketSize) {
		super(ByteOrder.LITTLE_ENDIAN, maxPacketSize, 0, 2, -2, 2, true);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		if (!ctx.channel().isActive()) {
			in.skipBytes(in.readableBytes());
			return null;
		}
		return super.decode(ctx, in);
	}
}
