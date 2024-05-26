package com.aionemu.chatserver.configs.network;

import java.net.InetSocketAddress;

import com.aionemu.commons.configuration.Property;

public class NetworkConfig {

	/**
	 * Address where Aion clients will attempt to connect to (host/domain name or IP)
	 */
	@Property(key = "chatserver.network.client.connect_address", defaultValue = "0.0.0.0:10241")
	public static InetSocketAddress CLIENT_CONNECT_ADDRESS;

	/**
	 * Local address where CS will listen for Aion client connections (0.0.0.0 = bind any local IP)
	 */
	@Property(key = "chatserver.network.client.socket_address", defaultValue = "0.0.0.0:10241")
	public static InetSocketAddress CLIENT_SOCKET_ADDRESS;

	/**
	 * Local address where CS will listen for GS connections (0.0.0.0 = bind any local IP)
	 */
	@Property(key = "chatserver.network.gameserver.socket_address", defaultValue = "0.0.0.0:9021")
	public static InetSocketAddress GAMESERVER_SOCKET_ADDRESS;

	/**
	 * Password for GS authentication
	 */
	@Property(key = "chatserver.network.gameserver.password", defaultValue = "")
	public static String GAMESERVER_PASSWORD;

	/**
	 * Number of threads dedicated to be doing io read & write. There is always 1 acceptor thread. If value is < 1 - acceptor thread will also handle
	 * read & write. If value is > 0 - there will be given amount of read & write threads + 1 acceptor thread.
	 */
	@Property(key = "chatserver.network.nio.threads", defaultValue = "1")
	public static int NIO_READ_WRITE_THREADS;
}
