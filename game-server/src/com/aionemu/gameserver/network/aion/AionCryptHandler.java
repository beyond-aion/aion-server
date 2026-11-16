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
	/**
	 * Capacity of the client's send buffer, so the most it can hold over from its previous connection.
	 */
	private static final int MAX_LEFTOVER_BYTES = 66560;

	private final AionConnection connection;
	private final Crypt crypt;
	private boolean decryptedAny;
	private int leftoverBytes;
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
		if (!crypt.isEnabled()) { // see below, data the client queued for its previous connection
			frame.release();
			return;
		}
		int frameSize = frame.readableBytes() + 2;
		if (!crypt.decrypt(frame)) {
			frame.release();
			// The client doesn't clear its send buffer when a connection breaks, and sends what it still holds over its next connection, encrypted
			// with the old key (e.g. after a server restart and reconnecting from the server list). They come before the version check and the time
			// sync, which the client builds right after receiving SM_KEY, with the new key. Counting them as decryption failures disconnected the
			// client in the middle of the login, so they are dropped, up to what the client's send buffer can hold. Nothing waits for an answer to
			// them: they belong to the session the client already left for the server list. Not tested with a real client yet; if clients hang
			// after reconnecting, remove this branch to disconnect them again.
			if (!decryptedAny && (leftoverBytes += frameSize) <= MAX_LEFTOVER_BYTES) {
				log.debug("Dropped a packet of {} that the client encrypted for its previous connection", connection);
				return;
			}
			if (++corruptPackets >= MAX_CORRUPT_PACKETS_BEFORE_DISCONNECT) {
				log.warn("Client packet decryption failed {} times, disconnecting {}", corruptPackets, connection);
				ctx.close();
			} else {
				log.debug("[{}/{}] Decrypt fail, client packet passed...", corruptPackets, MAX_CORRUPT_PACKETS_BEFORE_DISCONNECT);
			}
			return;
		}
		decryptedAny = true;
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
