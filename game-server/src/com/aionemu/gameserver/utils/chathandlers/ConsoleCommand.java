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
		this(alias, "", "");
	}

	/**
	 * @see ConsoleCommand(String, String, String)
	 */
	public ConsoleCommand(String alias, String description) {
		this(alias, description, "");
	}

	/**
	 * Creates a new console command for use with the GM Panel (Shift + F1) or in macros if the console has been activated via
	 * {@code \con_disable_console 0} from the command tab of the GM Panel.
	 *
	 * @see ChatCommand#ChatCommand(String, String, String, String)
	 */
	public ConsoleCommand(String alias, String description, String syntaxInfo) {
		super(PREFIX, alias, description, syntaxInfo);
	}

	@Override
	public boolean validateAccess(Player player) {
		boolean hasAccess = player.hasAccess(getLevel()) || CommandsAccessService.hasAccess(player.getObjectId(), getAliasForLevel());
		if (!hasAccess && player.isStaff())
			sendInfo(player, "<You need access level " + getLevel() + " or higher to use " + getAliasWithPrefix() + ">");
		return hasAccess;
	}

	@Override
	boolean process(Player player, String... params) {
		if (!validateAccess(player))
			return player.isStaff(); // return false for regular players, so chat will send entered text (this way you can't guess commands without rights)

		if (LoggingConfig.LOG_GMAUDIT)
			log.info("[Console Command] > [Player: " + player.getName() + "]"
				+ (player.getTarget() != null ? "[Target: " + player.getTarget().getName() + "]" : "") + ": " + getAliasWithPrefix() + " "
				+ String.join(" ", params));

		if (!run(player, params))
			sendInfo(player, "<Error while executing command>");

		return true;
	}
}
