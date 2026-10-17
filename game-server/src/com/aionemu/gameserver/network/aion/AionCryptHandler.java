package com.aionemu.gameserver.network.aion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.network.Crypt;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

/**
 * Decrypts the packets of an Aion game client and encrypts the packets sent to it. Runs on the I/O thread of the channel only, so the
 * encryption state needs no synchronization.
 */
class AionCryptHandler extends ChannelDuplexHandler {

	private static final Logger log = LoggerFactory.getLogger(AionCryptHandler.class);
	private static final int MAX_CORRUPT_PACKETS_BEFORE_DISCONNECT = 3;

	private final AionConnection connection;
	private final Crypt crypt;
	private int corruptPackets;

	AionCryptHandler(AionConnection connection, Crypt crypt) {
		this.connection = connection;
		this.crypt = crypt;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ByteBuf frame = (ByteBuf) msg;
		if (!frame.isReadable()) { // let the connection handler reject it
			ctx.fireChannelRead(frame);
			return;
		}
		if (!crypt.isEnabled()) { // the client sends crap upon reconnect, just before SM_KEY is sent
			frame.release();
			return;
		}
		if (!crypt.decrypt(frame)) {
			frame.release();
			if (++corruptPackets >= MAX_CORRUPT_PACKETS_BEFORE_DISCONNECT) {
				log.warn("Client packet decryption failed {} times, disconnecting {}", corruptPackets, connection);
				ctx.close();
			} else {
				log.debug("[{}/{}] Decrypt fail, client packet passed...", corruptPackets, MAX_CORRUPT_PACKETS_BEFORE_DISCONNECT);
			}
			return;
		}
		if (frame.readableBytes() < 5) { // op + static code + op == 5 bytes
			frame.release();
			log.warn("Received fake packet from {}, disconnecting", connection);
			ctx.close();
			return;
		}
		ctx.fireChannelRead(frame);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
		if (msg instanceof ByteBuf packet && packet.readableBytes() > 2) {
			try {
				crypt.encrypt(packet, packet.readerIndex() + 2); // everything after the size header
			} catch (RuntimeException e) {
				ReferenceCountUtil.release(packet);
				promise.setFailure(e);
				return;
			}
		}
		ctx.write(msg, promise);
	}
}
