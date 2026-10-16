package com.aionemu.chatserver.network.netty;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.packet.BaseClientPacket;
import com.aionemu.commons.network.packet.BaseServerPacket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.EventExecutor;

/**
 * Connection on top of a Netty channel. Received packets are executed one after another on the connection's packet executor, outside the I/O
 * threads.
 */
public abstract class NettyConnection<S extends BaseServerPacket> {

	private static final Logger log = LoggerFactory.getLogger(NettyConnection.class);

	private final Channel channel;
	private final EventExecutor packetExecutor;
	private final InetSocketAddress remoteAddress;
	private final AtomicInteger pendingPackets = new AtomicInteger();

	protected NettyConnection(Channel channel, EventExecutor packetExecutor) {
		this.channel = channel;
		this.packetExecutor = packetExecutor;
		this.remoteAddress = (InetSocketAddress) channel.remoteAddress();
	}

	public final void sendPacket(S packet) {
		if (!channel.isWritable() && channel.bytesBeforeWritable() > getMaxPendingWriteBytes()) {
			log.warn("{} has more than {} bytes waiting to be sent, disconnecting", this, getMaxPendingWriteBytes());
			close();
			return;
		}
		channel.writeAndFlush(packet, channel.voidPromise());
	}

	public final void close() {
		channel.close();
	}

	/**
	 * Sends the packet and closes the connection afterwards.
	 */
	public final void close(S packet) {
		channel.writeAndFlush(packet).addListener(ChannelFutureListener.CLOSE);
	}

	public final String getIP() {
		return remoteAddress.getAddress().getHostAddress();
	}

	public final boolean isFromLoopback() {
		return remoteAddress.getAddress().isLoopbackAddress();
	}

	final Channel getChannel() {
		return channel;
	}

	@SuppressWarnings("unchecked")
	final void encode(BaseServerPacket packet, ByteBuffer buffer) {
		writePacket((S) packet, buffer);
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
				packet.run();
			} finally {
				pendingPackets.decrementAndGet();
			}
		});
		return true;
	}

	/**
	 * Runs the task after all packets of this connection, which are already queued for execution.
	 */
	final void executeAfterPendingPackets(Runnable task) {
		packetExecutor.execute(task);
	}

	/**
	 * @return The packet for the given data, or null if it should be ignored.
	 */
	protected abstract BaseClientPacket<?> createPacket(ByteBuffer data);

	protected abstract void writePacket(S packet, ByteBuffer buffer);

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
