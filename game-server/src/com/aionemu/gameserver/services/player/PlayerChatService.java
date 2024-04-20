package com.aionemu.gameserver.services.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Source, Neon
 */
public class PlayerChatService {

	private static final Logger playerLog = LoggerFactory.getLogger("CHAT_LOG");
	private static final Logger gmLog = LoggerFactory.getLogger("ADMINAUDIT_LOG");

	public static boolean isFlooding(final Player player) {
		player.setLastMessageTime();

		if (player.floodMsgCount() > SecurityConfig.FLOOD_MSG)
			return true;

		return false;
	}

	public static void logWhisper(Player sender, Player receiver, String message) {
		logMessage(sender, ChatType.WHISPER, message, receiver);
	}

	public static void logMessage(Player sender, ChatType type, String message) {
		logMessage(sender, type, message, null);
	}

	private static void logMessage(Player sender, ChatType type, String message, Player receiver) {
		Logger log = playerLog;

		// log whisper to adminaudit.log, if GM is involved (ignores private chat logging settings)
		if (type == ChatType.WHISPER && (sender.isStaff() || (receiver != null && receiver.isStaff())) && LoggingConfig.LOG_GMAUDIT)
			log = gmLog;
		else {
			switch (type) {
				case WHISPER:
				case LEGION:
					if (!LoggingConfig.LOG_PRIVATE_CHATS)
						return;
					break;
				default:
					if (!LoggingConfig.LOG_GENERAL_CHATS)
						return;
			}
		}

		switch (type) {
			case WHISPER:
				log.info(String.format("[%s] - [%s]>[%s]: %s", type.toString(), sender.getName(), receiver != null ? receiver.getName() : "", message));
				break;
			case GROUP:
			case ALLIANCE:
			case GROUP_LEADER:
			case LEAGUE:
			case LEAGUE_ALERT:
				log.info(String.format("[%s] <%d> - [%s]: %s", type.toString(), sender.getCurrentTeamId(), sender.getName(), message));
				break;
			case LEGION:
				log.info(String.format("[%s] <%s> - [%s]: %s", type.toString(), sender.getLegion().getName(), sender.getName(), message));
				break;
			case NORMAL:
			case SHOUT:
			default:
				log.info(String.format("[%s] - [%s](%s): %s", type.toString(), sender.getName(), sender.getRace().toString(), message));
				break;
		}
	}
}
