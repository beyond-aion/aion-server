package com.aionemu.gameserver.network.loginserver;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.aionemu.commons.network.AConnection;
import com.aionemu.commons.network.ConnectionFactory;
import com.aionemu.commons.network.Dispatcher;

/**
 * ConnectionFactory implementation that will be creating AionConnections
 * 
 * @author -Nemesiss-
 */
public class LoginConnectionFactoryImpl implements ConnectionFactory {

	/**
	 * Create a new {@link com.aionemu.commons.network.AConnection AConnection} instance.<br>
	 * 
	 * @param socket
	 *          that new {@link com.aionemu.commons.network.AConnection AConnection} instance will represent.<br>
	 * @param dispatcher
	 *          to witch new connection will be registered.<br>
	 * @return a new instance of {@link com.aionemu.commons.network.AConnection AConnection}<br>
	 * @throws IOException
	 * @see com.aionemu.commons.network.AConnection
	 * @see com.aionemu.commons.network.Dispatcher
	 */

	/*
	 * (non-Javadoc)
	 * @see com.aionemu.commons.network.ConnectionFactory#create(java.nio.channels.SocketChannel, com.aionemu.commons.network.Dispatcher)
	 */
	@Override
	public AConnection create(SocketChannel socket, Dispatcher dispatcher) throws IOException {
		return new LoginServerConnection(socket, dispatcher);
	}
}
