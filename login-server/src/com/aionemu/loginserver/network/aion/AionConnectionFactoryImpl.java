package com.aionemu.loginserver.network.aion;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.aionemu.commons.network.AConnection;
import com.aionemu.commons.network.ConnectionFactory;
import com.aionemu.commons.network.Dispatcher;
import com.aionemu.loginserver.configs.Config;
import com.aionemu.loginserver.utils.FloodProtector;

/**
 * ConnectionFactory implementation that will be creating AionConnections
 * 
 * @author -Nemesiss-
 */
public class AionConnectionFactoryImpl implements ConnectionFactory {

	/**
	 * Create a new {@link com.aionemu.commons.network.AConnection AConnection} instance.<br>
	 * 
	 * @param socket
	 *          that new {@link com.aionemu.commons.network.AConnection AConnection} instance will represent.<br>
	 * @param dispatcher
	 *          to which new connection will be registered.<br>
	 * @return a new instance of {@link com.aionemu.commons.network.AConnection AConnection}<br>
	 * @throws IOException
	 * @see com.aionemu.commons.network.AConnection
	 * @see com.aionemu.commons.network.Dispatcher
	 */
	@Override
	public AConnection create(SocketChannel socket, Dispatcher dispatcher) throws IOException {
		if (Config.ENABLE_FLOOD_PROTECTION)
			if (FloodProtector.getInstance().tooFast(socket.socket().getInetAddress().getHostAddress()))
				return null;

		return new LoginConnection(socket, dispatcher);
	}
}
