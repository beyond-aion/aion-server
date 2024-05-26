package com.aionemu.commons.network;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.*;
import java.util.concurrent.Executor;

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
	private Dispatcher[] readWriteDispatchers;

	private int readWriteThreads;
	private ServerCfg[] cfgs;

	public NioServer(int readWriteThreads, ServerCfg... cfgs) {
		// Test if this build should use assertion and enforce it. If NetworkAssertion == false javac will remove this code block
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

	public void connect(Executor dcExecutor) {
		try {
			initDispatchers(readWriteThreads, dcExecutor);

			for (ServerCfg cfg : cfgs) {
				ServerSocketChannel serverChannel = ServerSocketChannel.open();
				serverChannel.configureBlocking(false);

				serverChannel.socket().bind(cfg.address());
				log.info("Listening on " + cfg.getAddressInfo() + " for " + cfg.clientDescription());

				// Register the server socket channel, indicating an interest in accepting new connections
				SelectionKey acceptKey = getAcceptDispatcher().register(serverChannel, SelectionKey.OP_ACCEPT, new Acceptor(cfg.connectionFactory(), this));
				serverChannelKeys.add(acceptKey);
			}
		} catch (Exception e) {
			throw new Error("Could not open server socket: " + e.getMessage(), e);
		}
	}

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

	private void initDispatchers(int readWriteThreads, Executor dcExecutor) throws IOException {
		if (readWriteThreads < 1) {
			acceptDispatcher = new AcceptReadWriteDispatcherImpl("AcceptReadWrite Dispatcher", dcExecutor);
			acceptDispatcher.start();
		} else {
			acceptDispatcher = new AcceptDispatcherImpl("Accept Dispatcher", dcExecutor);
			acceptDispatcher.start();

			readWriteDispatchers = new Dispatcher[readWriteThreads];
			for (int i = 0; i < readWriteDispatchers.length; i++) {
				readWriteDispatchers[i] = new AcceptReadWriteDispatcherImpl("ReadWrite-" + i + " Dispatcher", dcExecutor);
				readWriteDispatchers[i].start();
			}
		}
	}

	public final void shutdown() {
		log.info("Closing ServerChannels...");
		serverChannelKeys.forEach(SelectionKey::cancel);
		log.info("ServerChannels closed.");

		// find active connections once, at this point new ones cannot be added anymore
		Set<AConnection<?>> activeConnections = findAllConnections();
		if (!activeConnections.isEmpty()) {
			log.info("\tClosing " + activeConnections.size() + " connections...");

			// notify connections about server close (they should close themselves)
			activeConnections.forEach(AConnection::onServerClose);

			// wait for connections to close or force close them
			long timeout = System.currentTimeMillis() + 5000;
			while (isAnyConnectionClosePending(activeConnections)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ignored) {
				}
				if (System.currentTimeMillis() > timeout) {
					activeConnections.removeIf(AConnection::isClosed);
					log.info("\tForcing " + activeConnections.size() + " connections to disconnect...");
					activeConnections.forEach(AConnection::close);
					break;
				}
			}
			activeConnections.removeIf(AConnection::isClosed);
			log.info("\tActive connections left: " + activeConnections.size());
		}
	}

	private Set<AConnection<?>> findAllConnections() {
		Set<AConnection<?>> activeConnections = new HashSet<>();
		if (readWriteDispatchers != null) {
			for (Dispatcher d : readWriteDispatchers)
				for (SelectionKey key : d.selector().keys()) {
					if (key.attachment() instanceof AConnection<?> connection) {
						activeConnections.add(connection);
					}
				}
		}
		for (SelectionKey key : acceptDispatcher.selector().keys()) {
			if (key.attachment() instanceof AConnection<?> connection) {
				activeConnections.add(connection);
			}
		}
		return activeConnections;
	}

	private boolean isAnyConnectionClosePending(Collection<AConnection<?>> connections) {
		return connections.stream().anyMatch(AConnection::isPendingClose);
	}
}
