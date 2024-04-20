package com.aionemu.gameserver.services.ban;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.chatserver.ChatServer;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ViAl, Neon
 */
public class ChatBanService {

	/**
	 * List for player chat bans <player, expiration time>. Resets on server restart.
	 */
	private static final Map<Integer, Long> chatBans = new ConcurrentHashMap<>();

	/**
	 * Bans a player from all chats.
	 */
	public static void banPlayer(Player player, long durationMillis) {
		ChatServer.getInstance().sendPlayerGagPacket(player.getObjectId(), durationMillis);
		chatBans.put(player.getObjectId(), System.currentTimeMillis() + durationMillis);
		registerUnban(player, durationMillis);
	}

	public static void unbanPlayer(Player player) {
		player.getController().cancelTask(TaskId.GAG);
		ChatServer.getInstance().sendPlayerGagPacket(player.getObjectId(), 0);
		if (chatBans.remove(player.getObjectId()) != null && player.isOnline())
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CAN_CHAT_NOW());
	}

	private static void registerUnban(Player player, long delay) {
		player.getController().addTask(TaskId.GAG, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				unbanPlayer(player);
			}
		}, delay));
	}

	public static boolean isBanned(Player player) {
		return getBanMinutes(player) > 0;
	}

	/**
	 * Checks time left for the players ban.<br>
	 * If ban is over, this method automatically unbans the player.<br>
	 * If not and unban task is missing (e.g. due to logout), an unban task will be started.
	 * 
	 * @return The remaining ban time in minutes. Only returns 0 if ban time is really over.
	 */
	public static int getBanMinutes(Player player) {
		Long expireTime = chatBans.get(player.getObjectId());
		if (expireTime == null)
			return 0;

		long millisLeft = expireTime - System.currentTimeMillis();
		if (millisLeft <= 0) {
			unbanPlayer(player);
			return 0;
		}

		if (!player.getController().hasTask(TaskId.GAG))
			registerUnban(player, millisLeft);

		return (int) Math.max(0, Math.ceil(millisLeft / 60000f));
	}
}
