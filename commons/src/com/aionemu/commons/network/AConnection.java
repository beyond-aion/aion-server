package com.aionemu.commons.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.packet.BaseClientPacket;
import com.aionemu.commons.network.packet.BaseServerPacket;
import com.aionemu.commons.options.Assertion;

/**
 * Class that represent Connection with server socket. Connection is created by <code>ConnectionFactory</code> and attached to
 * <code>SelectionKey</code> key. Selection key is registered to one of Dispatchers <code>Selector</code> to handle io read and write.
 * 
 * @author -Nemesiss-
 */
public abstract class AConnection<T extends BaseServerPacket> {

	private static final Logger log = LoggerFactory.getLogger(AConnection.class);

	/**
	 * SocketChannel representing this connection
	 */
	private final SocketChannel socketChannel;
	/**
	 * Dispatcher [AcceptReadWriteDispatcherImpl] to which this connection SelectionKey is registered.
	 */
	private final Dispatcher dispatcher;
	/**
	 * SelectionKey representing this connection.
	 */
	private SelectionKey key;
	/**
	 * Time when the closing connection should be force-closed
	 */
	protected long pendingCloseUntilMillis;
	/**
	 * True if this connection is already closed.
	 */
	protected boolean closed;
	/**
	 * Object on which some methods are synchronized
	 */
	protected final Object guard = new Object();
	/**
	 * ByteBuffer for io write.
	 */
	public final ByteBuffer writeBuffer;
	/**
	 * ByteBuffer for io read.
	 */
	public final ByteBuffer readBuffer;

	/**
	 * Caching ip address to make sure that {@link #getIP()} method works even after disconnection
	 */
	private final String ip;

	/**
	 * Time when this connection was established
	 */
	private final long connectedAtMillis = System.currentTimeMillis();

	/**
	 * Time when data was last written to the socket or the send queue became non-empty
	 */
	private volatile long lastWriteProgressMillis;

	/**
	 * Client packets of this connection waiting for execution. Guarded by the lock of the {@link PacketProcessor}.
	 */
	final Deque<BaseClientPacket<? extends AConnection<?>>> pendingPackets = new ArrayDeque<>();

	/**
	 * True while this connection waits in the {@link PacketProcessor} or one of its packets is being executed. Guarded by the lock of the
	 * {@link PacketProcessor}.
	 */
	boolean scheduledForProcessing;

	/**
	 * Constructor
	 * 
	 * @param sc
	 * @param d
	 * @throws IOException
	 */
	public AConnection(SocketChannel sc, Dispatcher d, int rbSize, int wbSize) throws IOException {
		socketChannel = sc;
		dispatcher = d;
		writeBuffer = ByteBuffer.allocate(wbSize);
		writeBuffer.flip();
		writeBuffer.order(ByteOrder.LITTLE_ENDIAN);
		readBuffer = ByteBuffer.allocate(rbSize);
		readBuffer.order(ByteOrder.LITTLE_ENDIAN);

		this.ip = socketChannel.socket().getInetAddress().getHostAddress();
	}

	/**
	 * Set selection key - result of registration this AConnection socketChannel to one of dispatchers.
	 * 
	 * @param key
	 */
	final void setKey(SelectionKey key) {
		this.key = key;
	}

	/**
	 * @return SocketChannel representing this connection.
	 */
	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	/**
	 * Sends the ServerPacket to this client.
	 */
	public final void sendPacket(T serverPacket) {
		synchronized (guard) {
			if (pendingCloseUntilMillis != 0 || closed)
				return;

			if (isConnected()) {
				Queue<T> sendMsgQueue = getSendMsgQueue();
				if (sendMsgQueue.isEmpty()) {
					lastWriteProgressMillis = System.currentTimeMillis();
				} else if (isSendQueueStuck(sendMsgQueue.size())) {
					sendMsgQueue.clear();
					close();
					return;
				}
				sendMsgQueue.add(serverPacket);
				key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
				key.selector().wakeup();
			} else {
				close();
			}
		}
	}

	private boolean isSendQueueStuck(int queuedPackets) {
		if (queuedPackets >= getMaxSendQueueSize()) {
			log.warn("{} has {} server packets waiting to be sent, disconnecting", this, queuedPackets);
			return true;
		}
		long millisWithoutProgress = System.currentTimeMillis() - lastWriteProgressMillis;
		if (millisWithoutProgress > getMaxSendStallMillis()) {
			log.warn("{} hasn't received any data for {} ms while {} server packets are waiting to be sent, disconnecting", this, millisWithoutProgress,
				queuedPackets);
			return true;
		}
		return false;
	}

	/**
	 * @return The maximum number of server packets that may wait to be sent before the connection gets closed.
	 */
	protected int getMaxSendQueueSize() {
		return Integer.MAX_VALUE;
	}

	/**
	 * @return The maximum time in milliseconds the remote side may not receive any data while server packets are waiting to be sent, before the
	 *         connection gets closed.
	 */
	protected long getMaxSendStallMillis() {
		return Long.MAX_VALUE;
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
	 * Called by the Dispatcher Thread periodically to close connections which didn't authenticate in time or stopped receiving data.
	 */
	final void closeIfTimedOut(long nowMillis) {
		synchronized (guard) {
			if (pendingCloseUntilMillis != 0 || closed)
				return;
			long authTimeoutMillis = getAuthTimeoutMillis();
			if (authTimeoutMillis > 0 && nowMillis - connectedAtMillis > authTimeoutMillis && !isAuthenticated()) {
				log.info("{} didn't authenticate within {} ms, disconnecting", this, authTimeoutMillis);
				close();
				return;
			}
			Queue<T> sendMsgQueue = getSendMsgQueue();
			if (!sendMsgQueue.isEmpty() && isSendQueueStuck(sendMsgQueue.size())) {
				sendMsgQueue.clear();
				close();
			}
		}
	}

	/**
	 * Called by the Dispatcher after data was written to the socket.
	 */
	final void onDataWritten() {
		lastWriteProgressMillis = System.currentTimeMillis();
	}

	/**
	 * Connection will be closed at some time [by Dispatcher Thread], after that onDisconnect() method will be called to clear all other things.
	 */
	public final void close() {
		close(null);
	}

	/**
	 * Its guaranteed that closePacket will be sent before closing connection, but all past and future packets wont. Connection will be closed [by
	 * Dispatcher Thread], and onDisconnect() method will be called to clear all other things.
	 * 
	 * @param closePacket
	 *          Packet that will be sent before closing. If closePacket is null, regular {@link #close()} will be called instead.
	 */
	public final void close(T closePacket) {
		synchronized (guard) {
			if (pendingCloseUntilMillis != 0 || closed)
				return;

			pendingCloseUntilMillis = System.currentTimeMillis() + 2000;
			if (closePacket != null || !isConnected())
				getSendMsgQueue().clear();
			if (closePacket != null && isConnected()) {
				getSendMsgQueue().add(closePacket);
				key.interestOps(SelectionKey.OP_WRITE);
			}
			dispatcher.closeConnection(this);
			key.selector().wakeup(); // notify dispatcher
		}
	}

	protected abstract Queue<T> getSendMsgQueue();

	/**
	 * This will close the connection and call onDisconnect() on another thread. May be called only by Dispatcher Thread.
	 */
	final void disconnect(Executor dcExecutor) {
		// Test if this build should use assertion. If NetworkAssertion == false javac will remove this code block
		if (Assertion.NetworkAssertion)
			assert Thread.currentThread() == dispatcher;

		synchronized (guard) {
			if (closed)
				return;
			closed = true;
		}

		key.cancel();
		try {
			socketChannel.close();
		} catch (IOException ignored) {
		}
		key.attach(null);

		dcExecutor.execute(this::onDisconnect);
	}

	final boolean isConnected() {
		return key.isValid();
	}

	/**
	 * @return True if this connection is pendingClose and not closed yet.
	 */
	final boolean isPendingClose() {
		return pendingCloseUntilMillis != 0 && !closed;
	}

	final boolean isClosed() {
		return closed;
	}

	/**
	 * @return IP address of this Connection.
	 */
	public final String getIP() {
		return ip;
	}

	/**
	 * @param data
	 * @return True if data was processed correctly, False if some error occurred and connection should be closed NOW.
	 */
	protected abstract boolean processData(ByteBuffer data);

	/**
	 * This method will be called by Dispatcher, and will be repeated till return false.
	 * 
	 * @param data
	 * @return True if data was written to buffer, False indicating that there are not any more data to write.
	 */
	protected abstract boolean writeData(ByteBuffer data);

	/**
	 * Called when AConnection object is fully initialized and ready to process and send packets. It may be used as hook for sending first packet etc.
	 */
	protected abstract void initialized();

	/**
	 * This method is called to inform that this connection was closed and should be cleared. This method is called only once.
	 */
	protected abstract void onDisconnect();

	/**
	 * This method is called by NioServer to inform that NioServer is shouting down. This method is called only once.
	 */
	protected abstract void onServerClose();
}
