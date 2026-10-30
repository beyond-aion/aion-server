package com.aionemu.loginserver.network.aion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.network.ncrypt.CryptEngine;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

/**
 * Decrypts the packets of an Aion game client and verifies their checksum, and encrypts the packets sent to it. Runs on the I/O thread of the
 * channel only, so the encryption state needs no synchronization.
 */
class LoginCryptHandler extends ChannelDuplexHandler {

	private static final Logger log = LoggerFactory.getLogger(LoginCryptHandler.class);

	/**
	 * The checksum and the padding to whole Blowfish blocks that the encryption appends at most.
	 */
	private static final int MAX_ENCRYPTION_OVERHEAD = 16;

	private final LoginConnection connection;
	private final CryptEngine cryptEngine;

	LoginCryptHandler(LoginConnection connection, CryptEngine cryptEngine) {
		this.connection = connection;
		this.cryptEngine = cryptEngine;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ByteBuf frame = (ByteBuf) msg;
		if (!cryptEngine.decrypt(frame, frame.readerIndex(), frame.readableBytes())) {
			frame.release();
			log.warn("Wrong checksum from {}", connection);
			ctx.close();
			return;
		}
		frame.writerIndex(frame.writerIndex() - 8); // the checksum word and the unused word after it
		ctx.fireChannelRead(frame);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
		if (msg instanceof ByteBuf packet && packet.readableBytes() > 2) {
			try {
				int start = packet.readerIndex();
				packet.ensureWritable(MAX_ENCRYPTION_OVERHEAD);
				int size = cryptEngine.encrypt(packet, start + 2, packet.readableBytes() - 2) + 2;
				packet.writerIndex(start + size);
				packet.setShortLE(start, size);
			} catch (RuntimeException e) {
				ReferenceCountUtil.release(packet);
				promise.setFailure(e);
				return;
			}
		}
		ctx.write(msg, promise);
	}
}
