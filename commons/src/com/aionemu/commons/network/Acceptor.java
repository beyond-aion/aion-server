package com.aionemu.commons.network;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an <code>Acceptor</code> that will accept sockets<br>
 * connections dispatched by Accept <code>Dispatcher</code>. <code>Acceptor</code> is attachment<br>
 * of <code>ServerSocketChannel</code> <code>SelectionKey</code> registered on Accept <code>Dispatcher</code> <code>Selector</code>.<br>
 * <code>Acceptor</code> will create new <code>AConnection</code> object using <code>ConnectionFactory.create(SocketChannel socket)</code><br>
 * representing accepted socket, register it into one of ReadWrite <code>Dispatcher</code><br>
 * <code>Selector</code> as ready for io read operations.<br>
 * 
 * @author -Nemesiss-
 * @see com.aionemu.commons.network.Dispatcher
 * @see java.nio.channels.ServerSocketChannel
 * @see java.nio.channels.SelectionKey
 * @see java.nio.channels.SocketChannel
 * @see java.nio.channels.Selector
 * @see com.aionemu.commons.network.AConnection
 * @see com.aionemu.commons.network.ConnectionFactory
 * @see com.aionemu.commons.network.NioServer
 */
public class Acceptor {

	private static final Logger log = LoggerFactory.getLogger(Acceptor.class);

	private final ServerCfg cfg;

	/**
	 * <code>NioServer</code> that created this Acceptor.
	 * 
	 * @see com.aionemu.commons.network.NioServer
	 */
	private final NioServer nioServer;

	/**
	 * Number of open connections per IP address, only tracked if {@link ServerCfg#maxConnectionsPerIp()} is set
	 */
	private final Map<String, Integer> connectionsByIp = new ConcurrentHashMap<>();

	Acceptor(ServerCfg cfg, NioServer nioServer) {
		this.cfg = cfg;
		this.nioServer = nioServer;
	}

	/**
	 * Method called by Accept <code>Dispatcher</code> <code>Selector</code> when socket<br>
	 * connects to <code>ServerSocketChannel</code> listening for connections.<br>
	 * New instance of <code>AConnection</code> will be created by <code>ConnectionFactory</code>,<br>
	 * socket representing accepted connection will be register into<br>
	 * one of ReadWrite <code>Dispatchers</code> <code>Selector as ready for io read operations.<br>
	 * 
	 * @param key
	 *          <code>SelectionKey</code> representing <code>ServerSocketChannel</code> that is accepting<br>
	 *          new socket connection.
	 * @throws IOException
	 * @see com.aionemu.commons.network.Dispatcher
	 * @see java.nio.channels.ServerSocketChannel
	 * @see java.nio.channels.SelectionKey
	 * @see java.nio.channels.SocketChannel
	 * @see java.nio.channels.Selector
	 * @see com.aionemu.commons.network.AConnection
	 * @see com.aionemu.commons.network.ConnectionFactory
	 */
	public final void accept(SelectionKey key) throws IOException {
		// For an accept to be pending the channel must be a server socket channel
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();
		if (socketChannel == null)
			return;

		String ip = socketChannel.socket().getInetAddress().getHostAddress();
		if (!tryAcquireConnectionSlot(ip)) {
			log.debug("Rejected connection from {}: limit of {} connections per IP reached", ip, cfg.maxConnectionsPerIp());
			socketChannel.close();
			return;
		}

		AConnection<?> con;
		try {
			socketChannel.configureBlocking(false);
			socketChannel.socket().setSoLinger(true, 10);
			socketChannel.socket().setTcpNoDelay(true);

			Dispatcher dispatcher = nioServer.getReadWriteDispatcher();
			con = cfg.connectionFactory().create(socketChannel, dispatcher);

			if (con == null) {
				releaseConnectionSlot(ip);
				socketChannel.close();
				return;
			}

			if (cfg.maxConnectionsPerIp() > 0)
				con.setOnCloseCallback(() -> releaseConnectionSlot(ip));
			// register
			dispatcher.register(socketChannel, SelectionKey.OP_READ, con);
		} catch (IOException | RuntimeException e) {
			releaseConnectionSlot(ip);
			socketChannel.close();
			throw e;
		}
		// notify initialized :)
		con.initialized();
	}

	private boolean tryAcquireConnectionSlot(String ip) {
		if (cfg.maxConnectionsPerIp() <= 0)
			return true;
		boolean[] acquired = { false };
		connectionsByIp.compute(ip, (_, count) -> {
			int current = count == null ? 0 : count;
			if (current >= cfg.maxConnectionsPerIp())
				return count;
			acquired[0] = true;
			return current + 1;
		});
		return acquired[0];
	}

	private void releaseConnectionSlot(String ip) {
		if (cfg.maxConnectionsPerIp() > 0)
			connectionsByIp.computeIfPresent(ip, (_, count) -> count > 1 ? count - 1 : null);
	}
}
