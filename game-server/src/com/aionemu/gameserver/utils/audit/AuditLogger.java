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
	 * Replaces control characters so a client cannot forge log lines, and chat markup when the message goes to the staff chat.
	 */
	private static String sanitize(String message, boolean escapeMarkup) {
		if (message == null)
			return "";
		StringBuilder sb = new StringBuilder(Math.min(message.length(), 256));
		for (int i = 0; i < message.length() && sb.length() < 256; i++) {
			char c = message.charAt(i);
			if (c < ' ' || c == '\u007F')
				sb.append(' ');
			else if (escapeMarkup && (c == '[' || c == '$'))
				sb.append('(');
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
		if (PunishmentConfig.PUNISHMENT_ENABLE)
			AutoBan.punishment(player);

		if (LoggingConfig.LOG_AUDIT)
			log.info("{} {}", player, sanitize(message, false));

		for (Player gm : GMService.getInstance().getOnlineStaffMembers()) {
			if (gm.hasAccess(AdminConfig.AUDIT_INFO))
				PacketSendUtility.sendMessage(gm, ChatUtil.charName(player) + " " + sanitize(message, true), ChatType.YELLOW);
		}
	}
}
