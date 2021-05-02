package com.aionemu.gameserver.network.aion;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.ConnectionFactory;
import com.aionemu.commons.network.Dispatcher;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.network.sequrity.FloodManager;
import com.aionemu.gameserver.network.sequrity.FloodManager.Result;

/**
 * ConnectionFactory implementation that will be creating AionConnections
 * 
 * @author -Nemesiss-
 */
public class GameConnectionFactoryImpl implements ConnectionFactory {

	private final Logger log = LoggerFactory.getLogger(GameConnectionFactoryImpl.class);
	private FloodManager floodAcceptor;

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

	public GameConnectionFactoryImpl() {
		if (NetworkConfig.ENABLE_FLOOD_CONNECTIONS) {
			floodAcceptor = new FloodManager(NetworkConfig.Flood_Tick, new FloodManager.FloodFilter(NetworkConfig.Flood_SWARN, NetworkConfig.Flood_SReject,
				NetworkConfig.Flood_STick), // short period
				new FloodManager.FloodFilter(NetworkConfig.Flood_LWARN, NetworkConfig.Flood_LReject, NetworkConfig.Flood_LTick)); // long period
		}
	}

	@Override
	public AionConnection create(SocketChannel socket, Dispatcher dispatcher) throws IOException {
		if (NetworkConfig.ENABLE_FLOOD_CONNECTIONS) {
			String host = socket.socket().getInetAddress().getHostAddress();
			final Result isFlooding = floodAcceptor.isFlooding(host, true);
			switch (isFlooding) {
				case REJECTED:
					log.warn("Rejected connection from " + host);
					return null;
				case WARNED:
					log.warn("Connection over warn limit from " + host);
					break;
			}
		}

		return new AionConnection(socket, dispatcher);
	}
}
