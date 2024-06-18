package com.aionemu.commons.network;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * This is implementation of <code>Dispatcher</code> that may accept connections, read and write data.
 * 
 * @author -Nemesiss-
 * @see com.aionemu.commons.network.Dispatcher
 * @see java.nio.channels.Selector
 */
public class AcceptReadWriteDispatcherImpl extends Dispatcher {

	/**
	 * List of connections that should be closed by this <code>Dispatcher</code> as soon as possible.
	 */
	private final List<AConnection<?>> pendingClose = new ArrayList<>();

	public AcceptReadWriteDispatcherImpl(String name, Executor dcExecutor) throws IOException {
		super(name, dcExecutor);
	}

	/**
	 * Process Pending Close connections and then dispatch <code>Selector</code> selected-key set.
	 * 
	 * @see com.aionemu.commons.network.Dispatcher#dispatch()
	 */
	@Override
	void dispatch() throws IOException {
		int selected = selector.select();

		if (selected != 0) {
			for (Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator(); selectedKeys.hasNext();) {
				SelectionKey key = selectedKeys.next();
				selectedKeys.remove();

				if (!key.isValid())
					continue;

				// Check what event is available and deal with it
				switch (key.readyOps()) {
					case SelectionKey.OP_ACCEPT -> accept(key);
					case SelectionKey.OP_READ -> read(key);
					case SelectionKey.OP_WRITE -> write(key);
					case SelectionKey.OP_READ | SelectionKey.OP_WRITE -> {
						read(key);
						if (key.isValid())
							write(key);
					}
				}
			}
		}
		processPendingClose();
	}

	/**
	 * Add connection to pendingClose list, so this connection will be closed by this <code>Dispatcher</code> as soon as possible.
	 * 
	 * @see com.aionemu.commons.network.Dispatcher#closeConnection(com.aionemu.commons.network.AConnection)
	 */
	@Override
	void closeConnection(AConnection<?> con) {
		synchronized (pendingClose) {
			pendingClose.add(con);
		}
	}

	/**
	 * Process Pending Close connections.
	 */
	private void processPendingClose() {
		if (pendingClose.isEmpty())
			return;
		synchronized (pendingClose) {
			long nowMillis = System.currentTimeMillis();
			for (Iterator<AConnection<?>> iterator = pendingClose.iterator(); iterator.hasNext();) {
				AConnection<?> connection = iterator.next();
				if (connection.getSendMsgQueue().isEmpty() || !connection.isConnected() || nowMillis > connection.pendingCloseUntilMillis) {
					closeConnectionImpl(connection);
					iterator.remove();
				}
			}
		}
	}
}
