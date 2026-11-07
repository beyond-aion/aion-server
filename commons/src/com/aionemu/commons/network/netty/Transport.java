package com.aionemu.commons.network.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.IoHandlerFactory;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.uring.IoUring;
import io.netty.channel.uring.IoUringIoHandler;
import io.netty.channel.uring.IoUringServerSocketChannel;
import io.netty.channel.uring.IoUringSocketChannel;

/**
 * Socket transport of the network threads.
 */
public enum Transport {

	/**
	 * epoll on Linux, NIO elsewhere. io_uring is deliberately not part of it: it has to be chosen explicitly.
	 */
	AUTO,
	IO_URING,
	EPOLL,
	NIO;

	private static final Logger log = LoggerFactory.getLogger(Transport.class);

	/**
	 * @return The implementation of this transport. Any other than {@link #AUTO} is used without a fallback.
	 * @throws IllegalStateException
	 *           if the transport is not available on this system
	 */
	Implementation select() {
		Implementation implementation = switch (this) {
			case AUTO -> {
				if (Epoll.isAvailable())
					yield Implementation.EPOLL;
				if (System.getProperty("os.name").startsWith("Linux"))
					log.warn("The epoll transport is unavailable, falling back to NIO: {}", Epoll.unavailabilityCause().toString());
				yield Implementation.NIO;
			}
			case IO_URING -> {
				if (!IoUring.isAvailable())
					throw new IllegalStateException("The io_uring transport is unavailable", IoUring.unavailabilityCause());
				yield Implementation.IO_URING;
			}
			case EPOLL -> {
				if (!Epoll.isAvailable())
					throw new IllegalStateException("The epoll transport is unavailable", Epoll.unavailabilityCause());
				yield Implementation.EPOLL;
			}
			case NIO -> Implementation.NIO;
		};
		log.info("Using the {} transport", implementation.displayName);
		return implementation;
	}

	enum Implementation {

		IO_URING("io_uring", IoUringServerSocketChannel.class, IoUringSocketChannel.class),
		EPOLL("epoll", EpollServerSocketChannel.class, EpollSocketChannel.class),
		NIO("NIO", NioServerSocketChannel.class, NioSocketChannel.class);

		private final String displayName;
		final Class<? extends ServerSocketChannel> serverChannelClass;
		final Class<? extends SocketChannel> channelClass;

		Implementation(String displayName, Class<? extends ServerSocketChannel> serverChannelClass, Class<? extends SocketChannel> channelClass) {
			this.displayName = displayName;
			this.serverChannelClass = serverChannelClass;
			this.channelClass = channelClass;
		}

		IoHandlerFactory newIoHandlerFactory() {
			return switch (this) {
				case IO_URING -> IoUringIoHandler.newFactory();
				case EPOLL -> EpollIoHandler.newFactory();
				case NIO -> NioIoHandler.newFactory();
			};
		}
	}
}
