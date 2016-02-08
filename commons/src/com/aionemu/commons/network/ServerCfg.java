package com.aionemu.commons.network;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * This class represents ServerCfg for configuring NioServer
 * 
 * @author -Nemesiss-, Neon
 */
public class ServerCfg {

	private InetSocketAddress address;
	private String connectionName;
	private final ConnectionFactory factory;

	/**
	 * @param address
	 *          - host name/IP and port on which we will listen for connections.
	 * @param connectionName
	 *          - only for logging purposes.
	 * @param factory
	 *          - {@link ConnectionFactory} that will create {@link AConection} object, representing new socket connection.
	 */
	public ServerCfg(InetSocketAddress address, String connectionName, ConnectionFactory factory) {
		this.address = address;
		this.connectionName = connectionName;
		this.factory = factory;
	}

	public InetSocketAddress getSocketAddress() {
		return address;
	}

	public InetAddress getInetAddress() {
		return address.getAddress();
	}

	public String getIP() {
		return address.getAddress().getHostAddress();
	}

	public int getPort() {
		return address.getPort();
	}

	public String getConnectionName() {
		return connectionName;
	}

	public ConnectionFactory getConnectionFactory() {
		return factory;
	}
}
