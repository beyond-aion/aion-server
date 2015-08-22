package com.aionemu.gameserver.utils.chathandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.CommandsAccessService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author synchro2
 * @modified Neon
 */
public abstract class AdminCommand extends ChatCommand {

	public final static String PREFIX = "//";
	static final Logger log = LoggerFactory.getLogger("ADMINAUDIT_LOG");

	public AdminCommand(String alias) {
		this(alias, "");
	}

	public AdminCommand(String alias, String description) {
		super(alias, description, PREFIX);
	}

	@Override
	public boolean checkLevel(Player player) {
		return player.getAccessLevel() >= getLevel() || CommandsAccessService.getInstance().haveRigths(player.getObjectId(), getAlias());
	}

	@Override
	boolean process(Player player, String params) {

		if (!checkLevel(player)) {
			if (LoggingConfig.LOG_GMAUDIT)
				log.info("[ADMIN COMMAND] > [Player: " + player.getName() + "] has tried to use the command " + getAliasWithPrefix()
					+ " without having the rights");
			if (player.isGM()) {
				PacketSendUtility.sendMessage(player, "[WARN] You need to have access level " + getLevel() + " or more to use "
					+ getAliasWithPrefix());
				return true;
			}
			// return false so chat will send this message (this way you can't guess admin commands without rights)
			return false;
		}

		boolean success = false;
		if (params.isEmpty())
			success = this.run(player, EMPTY_PARAMS);
		else
			success = this.run(player, params.split(" "));

		if (LoggingConfig.LOG_GMAUDIT)
			log.info("[ADMIN COMMAND] > [Name: " + player.getName() + "]"
				+ (player.getTarget() != null ? "[Target : " + player.getTarget().getName() + "]" : "") + ": " + getAliasWithPrefix() + " "
				+ params);

		if (!success) {
			PacketSendUtility.sendMessage(player, "<You have failed to execute " + getAliasWithPrefix() + ">");
			return true;
		}
		else
			return success;
	}
}
