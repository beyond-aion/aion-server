package com.aionemu.chatserver.configs;

import java.net.InetSocketAddress;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.configs.CommonsConfig;
import com.aionemu.commons.configs.DatabaseConfig;
import com.aionemu.commons.configuration.ConfigurableProcessor;
import com.aionemu.commons.configuration.Property;
import com.aionemu.commons.utils.PropertiesUtils;

/**
 * @author ATracer
 */
public class Config {

	protected static final Logger log = LoggerFactory.getLogger(Config.class);

	/**
	 * Address where Aion clients will attempt to connect to (host/domain name or IP)
	 */
	@Property(key = "chatserver.network.client.connect_address", defaultValue = "127.0.0.1:10241")
	public static InetSocketAddress CLIENT_CONNECT_ADDRESS;

	/**
	 * Local address where CS will listen for Aion client connections (* = bind any local IP)
	 */
	@Property(key = "chatserver.network.client.socket_address", defaultValue = "*:10241")
	public static InetSocketAddress CLIENT_SOCKET_ADDRESS;

	/**
	 * Local address where CS will listen for GS connections (* = bind any local IP)
	 */
	@Property(key = "chatserver.network.gameserver.socket_address", defaultValue = "*:9021")
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

	/**
	 * Log requests to new channels
	 */
	@Property(key = "chatserver.log.channel.request", defaultValue = "false")
	public static boolean LOG_CHANNEL_REQUEST;

	/**
	 * Log requests to invalid channels
	 */
	@Property(key = "chatserver.log.channel.invalid", defaultValue = "false")
	public static boolean LOG_CHANNEL_INVALID;

	/**
	 * Log Chat
	 */
	@Property(key = "chatserver.log.chat", defaultValue = "false")
	public static boolean LOG_CHAT;

	/**
	 * Log Chat and Save to Database
	 */
	@Property(key = "chatserver.log.chat_to_db", defaultValue = "false")
	public static boolean LOG_CHAT_TO_DB;

	/**
	 * Message Delay
	 */
	@Property(key = "chatserver.chat.message.delay", defaultValue = "30")
	public static int MESSAGE_DELAY;

	/**
	 * Specifies the frequency the chat server will be restarted
	 */
	@Property(key = "chatserver.restart.frequency", defaultValue = "NEVER")
	public static String CHATSERVER_RESTART_FREQUENCY;

	/**
	 * Specifies the exact time of day the server should be restarted (of course respecting the frequency)
	 */
	@Property(key = "chatserver.restart.time", defaultValue = "5:00")
	public static String CHATSERVER_RESTART_TIME;

	/**
	 * Load configs from files.
	 */
	public static void load() {
		try {
			Properties myProps = null;
			try {
				log.info("Loading: ./config/mycs.properties");
				myProps = PropertiesUtils.load("./config/mycs.properties");
			} catch (Exception e) {
				log.info("No override properties found");
			}

			String network = "./config/network";
			log.info("Loading: " + network + "/*");
			Properties[] props = PropertiesUtils.loadAllFromDirectory(network);
			PropertiesUtils.overrideProperties(props, myProps);

			ConfigurableProcessor.process(Config.class, props);
			ConfigurableProcessor.process(CommonsConfig.class, props);
			ConfigurableProcessor.process(DatabaseConfig.class, props);

		} catch (Exception e) {
			throw new Error("Can't load chatserver configuration:", e);
		}
	}
}
