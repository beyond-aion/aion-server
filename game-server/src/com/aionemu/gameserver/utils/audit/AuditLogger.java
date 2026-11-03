package com.aionemu.gameserver.utils.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.configs.main.PunishmentConfig;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke, Neon
 */
public class AuditLogger {

	private static final Logger log = LoggerFactory.getLogger("AUDIT_LOG");

	/**
	 * Replaces control characters so a client cannot forge log lines
	 */
	private static String sanitize(String message) {
		if (message == null)
			return "";
		StringBuilder sb = new StringBuilder(message.length());
		for (int i = 0; i < message.length(); i++) {
			char c = message.charAt(i);
			if (c == '\n')
				sb.append("\\n");
			else if (c == '\r')
				sb.append("\\r");
			else if (ChatUtil.isInvalidCharacter(c))
				sb.append("\\u%04x".formatted((int) c));
			else
				sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * Logs message, if audit log is enabled.<br>
	 * Notifies permitted online staff members.<br>
	 * Automatically punishes player, if punishments are enabled.
	 */
	public static void log(Player player, String message) {
		message = sanitize(message);
		if (PunishmentConfig.PUNISHMENT_ENABLE)
			AutoBan.punishment(player);

		if (LoggingConfig.LOG_AUDIT)
			log.info("{} {}", player, message);

		for (Player gm : GMService.getInstance().getOnlineStaffMembers()) {
			if (gm.hasAccess(AdminConfig.AUDIT_INFO))
				PacketSendUtility.sendMessage(gm, ChatUtil.charName(player) + " " + message, ChatType.YELLOW);
		}
	}
}
