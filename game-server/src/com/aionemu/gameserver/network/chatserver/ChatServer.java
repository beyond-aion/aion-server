package com.aionemu.gameserver.network.chatserver;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.Dispatcher;
import com.aionemu.commons.network.NioServer;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.chatserver.ChatServerConnection.State;
import com.aionemu.gameserver.network.chatserver.serverpackets.SM_CS_PLAYER_AUTH;
import com.aionemu.gameserver.network.chatserver.serverpackets.SM_CS_PLAYER_GAG;
import com.aionemu.gameserver.network.chatserver.serverpackets.SM_CS_PLAYER_LOGOUT;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer, Neon
 */
public class ChatServer {

	private static final Logger log = LoggerFactory.getLogger(ChatServer.class);
	private byte[] publicIp = new byte[0];
	private int publicPort = 0;
	private ChatServerConnection csCon = null;
	private NioServer nioServer = null;

	public static final ChatServer getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * Prevent instantiation.
	 */
	private ChatServer() {
	}

	public void connect(NioServer nioServer) {
		if (csCon != null)
			throw new IllegalStateException("Chat server is already connected.");

		try {
			this.nioServer = nioServer;
			SocketChannel sc = SocketChannel.open(NetworkConfig.CHAT_ADDRESS);
			sc.configureBlocking(false);
			Dispatcher d = nioServer.getReadWriteDispatcher();
			csCon = new ChatServerConnection(sc, d);
			d.register(sc, SelectionKey.OP_READ, csCon);
			csCon.initialized();
		} catch (IOException e) {
			csCon = null;
			int delay;
			if (e instanceof SocketException) {
				delay = 10;
				log.info("Could not connect to chat server at " + NetworkConfig.CHAT_ADDRESS + ", trying again in " + delay + "s");
			} else {
				delay = 60;
				log.error("Could not connect to chat server at " + NetworkConfig.CHAT_ADDRESS + ", trying again in " + delay + "s", e);
			}
			ThreadPoolManager.getInstance().schedule(() -> connect(nioServer), delay * 1000);
		}
	}

	public void disconnect() {
		if (csCon != null) {
			csCon.close();
			csCon = null;
		}
		setPublicAddress(new byte[0], 0);
	}

	public void reconnect() {
		if (csCon == null)
			return;
		int delay = csCon.getState() == State.AUTHED ? 5 : 15;
		disconnect();
		log.info("Reconnecting to chat server in " + delay + "s...");
		ThreadPoolManager.getInstance().schedule(() -> connect(nioServer), delay * 1000);
	}

	public boolean isUp() {
		return csCon != null && csCon.getState() == State.AUTHED;
	}

	public void setPublicAddress(byte[] ip, int port) {
		this.publicIp = ip;
		this.publicPort = port;
	}

	public byte[] getPublicIP() {
		return publicIp;
	}

	public int getPublicPort() {
		return publicPort;
	}

	public void sendPlayerLoginRequest(Player player) {
		if (isUp())
			csCon.sendPacket(new SM_CS_PLAYER_AUTH(player));
	}

	public void sendPlayerLogout(Player player) {
		if (isUp())
			csCon.sendPacket(new SM_CS_PLAYER_LOGOUT(player.getObjectId()));
	}

	public void sendPlayerGagPacket(int playerObjId, long gagTime) {
		if (isUp())
			csCon.sendPacket(new SM_CS_PLAYER_GAG(playerObjId, gagTime));
	}

	private static class SingletonHolder {

		protected static final ChatServer instance = new ChatServer();
	}
}
