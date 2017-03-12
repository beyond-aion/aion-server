package com.aionemu.commons.network;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.options.Assertion;

/**
 * NioServer instance that handle connections on specified addresses.
 * 
 * @author -Nemesiss-
 */
public class NioServer {

	/**
	 * Logger for NioServer
	 */
	private static final Logger log = LoggerFactory.getLogger(NioServer.class.getName());

	/**
	 * The channels on which we'll accept connections
	 */
	private final List<SelectionKey> serverChannelKeys = new ArrayList<>();

	/**
	 * Dispatcher that will accept connections
	 */
	private Dispatcher acceptDispatcher;
	/**
	 * Useful int to load balance connections between Dispatchers
	 */
	private int currentReadWriteDispatcher;
	/**
	 * Read Write Dispatchers
	 */
	private Dispatcher[] readWriteDispatchers;

	/**
	 * 
	 */
	private int readWriteThreads;
	/**
	 * 
	 */
	private ServerCfg[] cfgs;

	/**
	 * Constructor.
	 * 
	 * @param readWriteThreads
	 *          - number of threads that will be used for handling read and write.
	 * @param cfgs
	 *          - Server Configurations
	 */
	public NioServer(int readWriteThreads, ServerCfg... cfgs) {
		/**
		 * Test if this build should use assertion and enforce it. If NetworkAssertion == false javac will remove this code block
		 */
		if (Assertion.NetworkAssertion) {
			boolean assertionEnabled = false;
			assert assertionEnabled = true;
			if (!assertionEnabled)
				throw new RuntimeException(
					"This is unstable build. Assertion must be enabled! Add -ea to your start script or consider using stable build instead.");
		}
		this.readWriteThreads = readWriteThreads;
		this.cfgs = cfgs;
	}

	public void connect() {
		try {
			this.initDispatchers(readWriteThreads);

			/** Create a new non-blocking server socket channel for clients */
			for (ServerCfg cfg : cfgs) {
				ServerSocketChannel serverChannel = ServerSocketChannel.open();
				serverChannel.configureBlocking(false);

				/** Bind the server socket to the specified address and port */
				serverChannel.socket().bind(cfg.getSocketAddress());
				log.info("Server listening on " + (cfg.getInetAddress().isAnyLocalAddress() ? "all interfaces," : "IP: " + cfg.getIP()) + " Port: "
					+ cfg.getPort() + " for " + cfg.getConnectionName());

				/**
				 * Register the server socket channel, indicating an interest in accepting new connections
				 */
				SelectionKey acceptKey = getAcceptDispatcher().register(serverChannel, SelectionKey.OP_ACCEPT,
					new Acceptor(cfg.getConnectionFactory(), this));
				serverChannelKeys.add(acceptKey);
			}
		} catch (Exception e) {
			log.error("NioServer Initialization Error: " + e, e);
			throw new Error("NioServer Initialization Error!");
		}
	}

	/**
	 * @return Accept Dispatcher.
	 */
	public final Dispatcher getAcceptDispatcher() {
		return acceptDispatcher;
	}

	/**
	 * @return one of ReadWrite Dispatcher or Accept Dispatcher if readWriteThreads was set to 0.
	 */
	public final Dispatcher getReadWriteDispatcher() {
		if (readWriteDispatchers == null)
			return acceptDispatcher;

		if (readWriteDispatchers.length == 1)
			return readWriteDispatchers[0];

		if (currentReadWriteDispatcher >= readWriteDispatchers.length)
			currentReadWriteDispatcher = 0;
		return readWriteDispatchers[currentReadWriteDispatcher++];
	}

	/**
	 * Initialize Dispatchers.
	 * 
	 * @param readWriteThreads
	 * @param packetExecutor
	 * @throws IOException
	 */
	private void initDispatchers(int readWriteThreads) throws IOException {
		if (readWriteThreads < 1) {
			acceptDispatcher = new AcceptReadWriteDispatcherImpl("AcceptReadWrite Dispatcher");
			acceptDispatcher.start();
		} else {
			acceptDispatcher = new AcceptDispatcherImpl("Accept Dispatcher");
			acceptDispatcher.start();

			readWriteDispatchers = new Dispatcher[readWriteThreads];
			for (int i = 0; i < readWriteDispatchers.length; i++) {
				readWriteDispatchers[i] = new AcceptReadWriteDispatcherImpl("ReadWrite-" + i + " Dispatcher");
				readWriteDispatchers[i].start();
			}
		}
	}

	/**
	 * Shutdown.
	 */
	public final void shutdown() {
		log.info("Closing ServerChannels...");
		try {
			for (SelectionKey key : serverChannelKeys)
				key.cancel();
			log.info("ServerChannel closed.");
		} catch (Exception e) {
			log.error("Error during closing ServerChannel, " + e, e);
		}

		// find active connections once, at this point new ones cannot be added anymore
		Set<AConnection<?>> activeConnections = findAllConnections();
		log.info("\tClosing " + activeConnections.size() + " active connections...");

		// notify connections about server close (they should close themselves)
		activeConnections.forEach(con -> con.onServerClose());

		// wait max 5s for connections to close, else force close
		long timeout = System.currentTimeMillis() + 5000;
		while (isAnyConnectionClosePending(activeConnections)) {
			if (System.currentTimeMillis() > timeout) {
				activeConnections.removeIf(con -> con.isClosed());
				log.info("\tForcing " + activeConnections.size() + " non responding connections to disconnect...");
				activeConnections.forEach(con -> con.close());
				timeout = System.currentTimeMillis() + 5000;
				while (isAnyConnectionClosePending(activeConnections) && timeout > System.currentTimeMillis()) {
				}
				break;
			}
		}
		log.info("\tActive connections left: " + findAllConnections().stream().filter(con -> !con.isClosed()).count());
	}

	private Set<AConnection<?>> findAllConnections() {
		Set<AConnection<?>> activeConnections = new HashSet<>();
		if (readWriteDispatchers != null) {
			for (Dispatcher d : readWriteDispatchers)
				for (SelectionKey key : d.selector().keys()) {
					if (key.attachment() instanceof AConnection) {
						activeConnections.add(((AConnection<?>) key.attachment()));
					}
				}
		} else {
			for (SelectionKey key : acceptDispatcher.selector().keys()) {
				if (key.attachment() instanceof AConnection) {
					activeConnections.add(((AConnection<?>) key.attachment()));
				}
			}
		}
		return activeConnections;
	}

	private boolean isAnyConnectionClosePending(Collection<AConnection<?>> connections) {
		return connections.stream().anyMatch(con -> con.isPendingClose());
	}
}
