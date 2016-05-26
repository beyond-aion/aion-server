package com.aionemu.gameserver.utils.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.configs.main.PunishmentConfig;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author MrPoke
 * @modified Neon
 */
public class AuditLogger {

	private static final Logger log = LoggerFactory.getLogger("AUDIT_LOG");

	/**
	 * Logs message, if audit log is enabled.<br>
	 * Sends message to GMs, if audit broadcast is enabled.<br>
	 * Automatically punishes player, if punishments are enabled.
	 */
	public static final void info(Player player, String message) {
		if (PunishmentConfig.PUNISHMENT_ENABLE)
			AutoBan.punishment(player, message);

		message = player + " " + message;

		if (LoggingConfig.LOG_AUDIT)
			log.info(message);

		if (SecurityConfig.GM_AUDIT_MESSAGE_BROADCAST)
			GMService.getInstance().broadcastMessageToGMs(message);
	}
}
