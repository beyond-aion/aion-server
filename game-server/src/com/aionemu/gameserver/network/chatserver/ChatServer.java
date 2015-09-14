package com.aionemu.gameserver.network.chatserver;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.Dispatcher;
import com.aionemu.commons.network.NioServer;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.chatserver.serverpackets.SM_CS_PLAYER_AUTH;
import com.aionemu.gameserver.network.chatserver.serverpackets.SM_CS_PLAYER_GAG;
import com.aionemu.gameserver.network.chatserver.serverpackets.SM_CS_PLAYER_LOGOUT;
import com.aionemu.gameserver.network.factories.CsPacketHandlerFactory;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public class ChatServer {

	private static final Logger log = LoggerFactory.getLogger(ChatServer.class);

	private ChatServerConnection chatServer;
	private NioServer nioServer;

	private boolean serverShutdown = false;

	public static final ChatServer getInstance() {
		return SingletonHolder.instance;
	}

	private ChatServer() {
	}

	public void setNioServer(NioServer nioServer) {
		this.nioServer = nioServer;
	}

	/**
	 * @return
	 */
	public ChatServerConnection connect() {
		SocketChannel sc;
		for (;;) {
			chatServer = null;
			log.info("Connecting to ChatServer: " + NetworkConfig.CHAT_ADDRESS);
			try {
				sc = SocketChannel.open(NetworkConfig.CHAT_ADDRESS);
				sc.configureBlocking(false);
				Dispatcher d = nioServer.getReadWriteDispatcher();
				CsPacketHandlerFactory csPacketHandlerFactory = new CsPacketHandlerFactory();
				chatServer = new ChatServerConnection(sc, d, csPacketHandlerFactory.getPacketHandler());

				// register
				d.register(sc, SelectionKey.OP_READ, chatServer);

				// initialized
				chatServer.initialized();

				return chatServer;
			} catch (Exception e) {
				log.info("Cant connect to ChatServer: " + e.getMessage());
			}
			try {
				/**
				 * 10s sleep
				 */
				Thread.sleep(10 * 1000);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * This method is called when we lost connection to ChatServer.
	 */
	public void chatServerDown() {
		log.warn("Connection with ChatServer lost...");

		chatServer = null;

		if (!serverShutdown) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					connect();
				}
			}, 5000);
		}
	}

	/**
	 * @param player
	 * @param token
	 */
	public void sendPlayerLoginRequst(Player player) {
		if (chatServer != null)
			chatServer.sendPacket(new SM_CS_PLAYER_AUTH(player.getObjectId(), player.getAcountName(), player.getName(), player.getAccessLevel(), player
				.getRace()));
	}

	/**
	 * @param player
	 */
	public void sendPlayerLogout(Player player) {
		if (chatServer != null)
			chatServer.sendPacket(new SM_CS_PLAYER_LOGOUT(player.getObjectId()));
	}

	public void sendPlayerGagPacket(int playerObjId, long gagTime) {
		if (chatServer != null)
			chatServer.sendPacket(new SM_CS_PLAYER_GAG(playerObjId, gagTime));
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		protected static final ChatServer instance = new ChatServer();
	}
}
