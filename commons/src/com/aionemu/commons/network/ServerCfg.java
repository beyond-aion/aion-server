package com.aionemu.commons.network;

import java.net.InetSocketAddress;

/**
 * This class represents ServerCfg for configuring NioServer
 * 
 * @author -Nemesiss-, Neon
 */
public record ServerCfg(InetSocketAddress address, String clientDescription, ConnectionFactory connectionFactory, int maxConnectionsPerIp) {

	/**
	 * Creates a config without a limit of connections per IP.
	 */
	public ServerCfg(InetSocketAddress address, String clientDescription, ConnectionFactory connectionFactory) {
		this(address, clientDescription, connectionFactory, 0);
	}

	public boolean isAnyLocalAddress() {
		return address.getAddress().isAnyLocalAddress();
	}

	public String getIP() {
		return address.getAddress().getHostAddress();
	}

	public int getPort() {
		return address.getPort();
	}

	public String getAddressInfo() {
		return (isAnyLocalAddress() ? "all addresses on port " : getIP() + ":") + getPort();
	}
}
