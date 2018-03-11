package com.aionemu.commons.network;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.concurrent.Executor;

/**
 * This is implementation of <code>Dispatcher</code> that may only accept connections.
 * 
 * @author -Nemesiss-
 * @see com.aionemu.commons.network.Dispatcher
 * @see java.nio.channels.Selector
 */
public class AcceptDispatcherImpl extends Dispatcher {

	public AcceptDispatcherImpl(String name, Executor dcExecutor) throws IOException {
		super(name, dcExecutor);
	}

	/**
	 * Dispatch <code>Selector</code> selected-key set.
	 * 
	 * @see com.aionemu.commons.network.Dispatcher#dispatch()
	 */
	@Override
	void dispatch() throws IOException {
		if (selector.select() != 0) {
			Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
			while (selectedKeys.hasNext()) {
				SelectionKey key = selectedKeys.next();
				selectedKeys.remove();

				if (key.isValid())
					accept(key);
			}
		}
	}

	/**
	 * This method should never be called on this implementation of <code>Dispatcher</code>
	 * 
	 * @throws UnsupportedOperationException
	 *           always!
	 * @see com.aionemu.commons.network.Dispatcher#closeConnection(com.aionemu.commons.network.AConnection)
	 */
	@Override
	void closeConnection(AConnection<?> con) {
		throw new UnsupportedOperationException("This method should never be called!");
	}
}
