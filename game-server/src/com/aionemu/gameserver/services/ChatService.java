package com.aionemu.gameserver.services;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CHAT_INIT;
import com.aionemu.gameserver.network.chatserver.ChatServer;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer
 */
public class ChatService {

	private static byte[] ip = { 127, 0, 0, 1 };
	private static int port = 10241;

	/**
	 * Disonnect from chat server
	 * 
	 * @param player
	 */
	public static void onPlayerLogout(Player player) {
		ChatServer.getInstance().sendPlayerLogout(player);
	}

	/**
	 * @param playerId
	 * @param token
	 * @param account
	 * @param nick
	 */
	public static void playerAuthed(int playerId, byte[] token) {
		Player player = World.getInstance().findPlayer(playerId);
		if (player != null) {
			PacketSendUtility.sendPacket(player, new SM_CHAT_INIT(token));
		}
	}

	/**
	 * @return the ip
	 */
	public static byte[] getIp() {
		return ip;
	}

	/**
	 * @return the port
	 */
	public static int getPort() {
		return port;
	}

	/**
	 * @param ip
	 *          the ip to set
	 */
	public static void setIp(byte[] _ip) {
		ip = _ip;
	}

	/**
	 * @param port
	 *          the port to set
	 */
	public static void setPort(int _port) {
		port = _port;
	}
}
