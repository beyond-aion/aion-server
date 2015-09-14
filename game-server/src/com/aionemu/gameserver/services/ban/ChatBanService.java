package com.aionemu.gameserver.services.ban;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.chatserver.ChatServer;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ViAl
 */
public class ChatBanService {

	private static final Logger log = LoggerFactory.getLogger(ChatBanService.class);
	/**
	 * accountId - expiration time
	 */
	private static final Map<Integer, Long> bannedAccounts = new HashMap<Integer, Long>();

	public static void onLogin(final Player player) {
		try {
			Long expireTime = bannedAccounts.get(player.getPlayerAccount().getId());
			if (expireTime == null)
				return;
			Long now = System.currentTimeMillis();
			if (now > expireTime) {
				deleteBan(player.getPlayerAccount().getId());
				return;
			}
			Long restTime = expireTime - now;
			banPlayer(player, restTime);
		} catch (Exception e) {
			log.error("Error while login, player " + player.getName(), e);
		}
	}

	public static void banPlayer(final Player player, Long time) {
		player.setGagged(true);
		Future<?> task = player.getController().getTask(TaskId.GAG);
		if (task != null)
			player.getController().cancelTask(TaskId.GAG);
		player.getController().addTask(TaskId.GAG, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				player.setGagged(false);
				PacketSendUtility.sendMessage(player, "You have been ungagged");
			}
		}, time));

		if (GSConfig.ENABLE_CHAT_SERVER)
			ChatServer.getInstance().sendPlayerGagPacket(player.getObjectId(), time);

		PacketSendUtility.sendMessage(player, "You are gagged for " + (time / 1000 / 60) + " minutes.");
	}

	public static void unbanPlayer(Player player) {
		player.setGagged(false);
		Future<?> task = player.getController().getTask(TaskId.GAG);
		if (task != null)
			player.getController().cancelTask(TaskId.GAG);
		PacketSendUtility.sendMessage(player, "You have been ungagged");
	}

	public static synchronized void deleteBan(Integer accountId) {
		bannedAccounts.remove(accountId);
	}

	public static synchronized void saveBan(Integer accountId, Long expireTime) {
		bannedAccounts.put(accountId, expireTime);
	}
}
