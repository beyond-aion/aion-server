package com.aionemu.gameserver.utils.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.configs.main.PunishmentConfig;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.google.common.base.Preconditions;

/**
 * @author MrPoke
 */
public class AuditLogger {

	private static final Logger log = LoggerFactory.getLogger("AUDIT_LOG");

	public static final void info(Player player, String message) {
		Preconditions.checkNotNull(player, "Player should not be null or use different info method");
		if (LoggingConfig.LOG_AUDIT) {
			info(player.getName(), player.getObjectId(), message);
		}
		if (PunishmentConfig.PUNISHMENT_ENABLE) {
			AutoBan.punishment(player, message);
		}
	}

	public static final void info(String playerName, int objectId, String message) {
			message += " Player name: " + playerName + " objectId: " + objectId;
			log.info(message);

			if (SecurityConfig.GM_AUDIT_MESSAGE_BROADCAST)
				GMService.getInstance().broadcastMesage(message);
	}
}
