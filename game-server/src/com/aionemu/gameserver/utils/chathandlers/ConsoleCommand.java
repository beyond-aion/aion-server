package com.aionemu.gameserver.utils.chathandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.CommandsAccessService;

/**
 * @author ginho1, Neon
 */
public abstract class ConsoleCommand extends ChatCommand {

	public final static String PREFIX = "";
	static final Logger log = LoggerFactory.getLogger("ADMINAUDIT_LOG");

	// only for backwards compatibility TODO: remove when all commands are updated
	public ConsoleCommand(String alias) {
		this(alias, "");
	}

	/**
	 * Registers a new console command.
	 * 
	 * @param alias
	 *          the command name
	 * @param description
	 *          description what the command does
	 */
	public ConsoleCommand(String alias, String description) {
		super(PREFIX, alias, description);
	}

	@Override
	public boolean validateAccess(Player player) {
		return player.hasAccess(getLevel()) || CommandsAccessService.hasAccess(player.getObjectId(), getAlias());
	}

	@Override
	boolean process(Player player, String... params) {

		if (!validateAccess(player)) {
			if (player.isStaff()) {
				sendInfo(player, "<You need access level " + getLevel() + " or higher to use " + getAliasWithPrefix() + ">");
				return true;
			}
			// return false so chat will send entered text (this way you can't guess commands without rights)
			return false;
		}

		if (LoggingConfig.LOG_GMAUDIT)
			log.info("[Console Command] > [Player: " + player.getName() + "]"
				+ (player.getTarget() != null ? "[Target: " + player.getTarget().getName() + "]" : "") + ": " + getAliasWithPrefix() + " "
				+ String.join(" ", params));

		if (!run(player, params))
			sendInfo(player, "<Error while executing command>");

		return true;
	}
}
