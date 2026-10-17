package com.aionemu.commons.network.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.packet.BaseClientPacket;
import com.aionemu.commons.network.packet.BaseServerPacket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * Connection on top of a Netty channel. Received packets are executed on the connection's packet executor, outside the I/O threads: one after
 * another if the executor runs one task at a time (a Netty {@code EventExecutor} or a {@link com.aionemu.commons.utils.concurrent.SequentialExecutor}),
 * concurrently if it is a thread pool.
 */
public abstract class NettyConnection<S extends BaseServerPacket> {

	private static final Logger log = LoggerFactory.getLogger(NettyConnection.class);

	/**
	 * Time a closing connection gets to send its remaining data, before it is closed anyway.
	 */
	private static final long CLOSE_TIMEOUT_MILLIS = 2000;

	private final Channel channel;
	private final Executor packetExecutor;
	private volatile InetSocketAddress remoteAddress;
	private final AtomicInteger pendingPackets = new AtomicInteger();
	private final AtomicBoolean closing = new AtomicBoolean();

	protected NettyConnection(Channel channel, Executor packetExecutor) {
		this.channel = channel;
		this.packetExecutor = packetExecutor;
		this.remoteAddress = (InetSocketAddress) channel.remoteAddress(); // null for outgoing connections, until they are established
	}

	/**
	 * Sends the packet, unless the connection is being closed.
	 */
	public final void sendPacket(S packet) {
		if (closing.get())
			return;
		if (!channel.isWritable() && channel.bytesBeforeWritable() > getMaxPendingWriteBytes()) {
			log.warn("{} has more than {} bytes waiting to be sent, disconnecting", this, getMaxPendingWriteBytes());
			channel.close();
			return;
		}
		channel.writeAndFlush(packet, channel.voidPromise());
	}

	/**
	 * Closes the connection after sending the packets that are already waiting to be sent. Packets sent afterwards are dropped.
	 */
	public final void close() {
		closeAfter(Unpooled.EMPTY_BUFFER);
	}

	/**
	 * Sends the packet and closes the connection afterwards. Packets sent afterwards are dropped.
	 */
	public final void close(S packet) {
		closeAfter(packet);
	}

	private void closeAfter(Object lastMessage) {
		if (!closing.compareAndSet(false, true))
			return;
		ChannelFuture lastWrite = channel.writeAndFlush(lastMessage);
		lastWrite.addListener(ChannelFutureListener.CLOSE);
		if (!lastWrite.isDone()) // a client that doesn't read would otherwise keep the connection open forever
			channel.eventLoop().schedule(() -> channel.close(), CLOSE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
	}

	/**
	 * @return True if the connection is closed or is being closed.
	 */
	public final boolean isClosing() {
		return closing.get() || !channel.isActive();
	}

	public final String getIP() {
		return remoteAddress.getAddress().getHostAddress();
	}

	public final boolean isFromLoopback() {
		return remoteAddress.getAddress().isLoopbackAddress();
	}

	/**
	 * @return The channel of this connection. Writing to it directly bypasses the checks of {@link #sendPacket}.
	 */
	protected final Channel getChannel() {
		return channel;
	}

	final void onChannelActive() {
		if (remoteAddress == null)
			remoteAddress = (InetSocketAddress) channel.remoteAddress();
		onConnect();
	}

	/**
	 * Writes the packet on an I/O thread. The same packet instance can be sent to several connections, whose I/O threads may differ, so it is
	 * written by one thread at a time.
	 */
	@SuppressWarnings("unchecked")
	final void encode(BaseServerPacket packet, ByteBuf out) {
		synchronized (packet) {
			try {
				writePacket((S) packet, out);
			} finally {
				packet.setBuf(null);
			}
		}
	}

	/**
	 * Queues the packet for execution after all previously received packets of this connection.
	 *
	 * @return False if the maximum number of packets is already waiting for execution.
	 */
	final boolean execute(BaseClientPacket<?> packet) {
		if (pendingPackets.incrementAndGet() > getMaxPendingPackets()) {
			pendingPackets.decrementAndGet();
			return false;
		}
		packetExecutor.execute(() -> {
			try {
				runPacket(packet);
			} finally {
				pendingPackets.decrementAndGet();
			}
		});
		return true;
	}

	/**
	 * Runs the task after all packets of this connection, which are already queued for execution, if the packet executor runs one task at a time.
	 */
	final void executeAfterPendingPackets(Runnable task) {
		packetExecutor.execute(task);
	}

	/**
	 * Executes the packet on a thread of the packet executor.
	 */
	protected void runPacket(BaseClientPacket<?> packet) {
		packet.run();
	}

	/**
	 * @return The packet for the given data, or null if it should be ignored. The data is only valid until the packet was read.
	 */
	protected abstract BaseClientPacket<?> createPacket(ByteBuf data);

	/**
	 * Writes the packet, including its size header, to the end of the buffer.
	 */
	protected abstract void writePacket(S packet, ByteBuf buffer);

	/**
	 * @return True if a packet that could not be read closes the connection, false if it is just ignored.
	 */
	protected boolean isReadFailureFatal() {
		return true;
	}

	/**
	 * @return True if the remote side has authenticated itself. Connections that didn't authenticate within {@link #getAuthTimeoutMillis()} get
	 *         closed.
	 */
	protected boolean isAuthenticated() {
		return true;
	}

	/**
	 * @return The time in milliseconds the remote side has to authenticate itself, or 0 for no limit.
	 */
	protected long getAuthTimeoutMillis() {
		return 0;
	}

	/**
	 * @return The maximum number of received packets that may wait for execution before the connection gets closed.
	 */
	protected abstract int getMaxPendingPackets();

	/**
	 * @return The maximum number of bytes that may wait to be sent before the connection gets closed.
	 */
	protected long getMaxPendingWriteBytes() {
		return Long.MAX_VALUE;
	}

	/**
	 * @return The maximum time in milliseconds the remote side may not receive data while the send buffer is full, before the connection gets
	 *         closed.
	 */
	protected long getMaxSendStallMillis() {
		return 0;
	}

	protected abstract void onConnect();

	/**
	 * Called once after the connection was closed and all of its pending packets were executed.
	 */
	protected abstract void onDisconnect();
}
