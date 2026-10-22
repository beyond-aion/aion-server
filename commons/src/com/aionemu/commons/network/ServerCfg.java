package com.aionemu.commons.network;

import java.net.InetSocketAddress;

import com.aionemu.commons.utils.NetworkUtils;

/**
 * This class represents ServerCfg for configuring NioServer
 * 
 * @author -Nemesiss-, Neon
 */
public record ServerCfg(InetSocketAddress address, String clientDescription, ConnectionFactory connectionFactory) {

	public String getIP() {
		return address.getAddress().getHostAddress();
	}

	public int getPort() {
		return address.getPort();
	}

	public String getAddressInfo() {
		return NetworkUtils.getAddressInfo(address);
	}
}
