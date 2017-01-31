package com.aionemu.loginserver.network.gameserver;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.aionemu.commons.network.ConnectionFactory;
import com.aionemu.commons.network.Dispatcher;

/**
 * ConnectionFactory implementation that will be creating GsConnections
 * 
 * @author -Nemesiss-
 */
public class GsConnectionFactoryImpl implements ConnectionFactory {

	/**
	 * Create a new {@link com.aionemu.commons.network.AConnection AConnection} instance.<br>
	 * 
	 * @param socket
	 *          that new {@link com.aionemu.commons.network.AConnection AConnection} instance will represent.<br>
	 * @param dispatcher
	 *          to wich new connection will be registered.<br>
	 * @return a new instance of {@link com.aionemu.commons.network.AConnection AConnection}<br>
	 * @throws IOException
	 * @see com.aionemu.commons.network.AConnection
	 * @see com.aionemu.commons.network.Dispatcher
	 */
	@Override
	public GsConnection create(SocketChannel socket, Dispatcher dispatcher) throws IOException {
		return new GsConnection(socket, dispatcher);
	}
}
