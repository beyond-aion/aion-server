package com.aionemu.chatserver.network.gameserver;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.aionemu.commons.network.AConnection;
import com.aionemu.commons.network.ConnectionFactory;
import com.aionemu.commons.network.Dispatcher;

/**
 * ConnectionFactory implementation that will be creating GsConnections
 * 
 * @author -Nemesiss-
 */
public class GsConnectionFactoryImpl implements ConnectionFactory {

	/**
	 * Create a new {@link AConnection} instance.<br>
	 * 
	 * @param socket
	 *          that new {@link AConnection} instance will represent.<br>
	 * @param dispatcher
	 *          to which new connection will be registered.<br>
	 * @return a new instance of {@link AConnection}<br>
	 * @see AConnection
	 * @see Dispatcher
	 */
	@Override
	public GsConnection create(SocketChannel socket, Dispatcher dispatcher) throws IOException {
		return new GsConnection(socket, dispatcher);
	}
}
