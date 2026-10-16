package com.aionemu.chatserver.network.netty;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.packet.BaseClientPacket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderException;

/**
 * Turns received frames into packets of its connection and enforces the authentication timeout and the send stall limit.
 */
class ConnectionHandler extends SimpleChannelInboundHandler<ByteBuf> {

	private static final Logger log = LoggerFactory.getLogger(ConnectionHandler.class);

	private final NettyConnection<?> connection;
	private int writabilityChanges;

	ConnectionHandler(NettyConnection<?> connection) {
		this.connection = connection;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		connection.onConnect();
		long authTimeoutMillis = connection.getAuthTimeoutMillis();
		if (authTimeoutMillis > 0) {
			ctx.executor().schedule(() -> {
				if (ctx.channel().isActive() && !connection.isAuthenticated()) {
					log.info("{} didn't authenticate within {} ms, disconnecting", connection, authTimeoutMillis);
					ctx.close();
				}
			}, authTimeoutMillis, TimeUnit.MILLISECONDS);
		}
		super.channelActive(ctx);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf frame) {
		if (!frame.isReadable()) {
			log.warn("{} sent an empty packet, disconnecting", connection);
			ctx.close();
			return;
		}
		BaseClientPacket<?> packet = connection.createPacket(frame.nioBuffer().order(ByteOrder.LITTLE_ENDIAN));
		if (packet == null)
			return;
		if (!packet.read()) {
			ctx.close();
			return;
		}
		if (!connection.execute(packet)) {
			log.warn("{} has more than {} packets waiting for execution, disconnecting", connection, connection.getMaxPendingPackets());
			ctx.close();
		}
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		int change = ++writabilityChanges;
		long stallMillis = connection.getMaxSendStallMillis();
		if (stallMillis > 0 && !ctx.channel().isWritable()) {
			ctx.executor().schedule(() -> {
				if (change == writabilityChanges && ctx.channel().isActive() && !ctx.channel().isWritable()) {
					log.warn("{} hasn't received data for {} ms while its send buffer is full, disconnecting", connection, stallMillis);
					ctx.close();
				}
			}, stallMillis, TimeUnit.MILLISECONDS);
		}
		super.channelWritabilityChanged(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		connection.executeAfterPendingPackets(connection::onDisconnect);
		super.channelInactive(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		if (cause instanceof DecoderException)
			log.warn("{} sent an invalid packet, disconnecting: {}", connection, cause.getMessage());
		else if (!(cause instanceof IOException))
			log.error("Error in connection of {}, disconnecting", connection, cause);
		ctx.close();
	}
}
