package com.aionemu.gameserver.utils.chathandlers;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.CommandsAccessService;

/**
 * @author synchro2, Neon
 */
public abstract class PlayerCommand extends ChatCommand {

	public final static String PREFIX = ".";

	/**
	 * @see PlayerCommand(String, String, String)
	 */
	public PlayerCommand(String alias, String description) {
		this(alias, description, "");
	}

	/**
	 * @see ChatCommand#ChatCommand(String, String, String, String)
	 */
	public PlayerCommand(String alias, String description, String syntaxInfo) {
		super(PREFIX, alias, description, syntaxInfo);
	}

	@Override
	public boolean validateAccess(Player player) {
		boolean hasAccess = player.hasPermission(getLevel()) || CommandsAccessService.hasAccess(player.getObjectId(), getAliasForLevel());
		if (!hasAccess && player.isStaff())
			sendInfo(player, "<You need membership level " + getLevel() + " or higher to use " + getAliasWithPrefix() + ">");
		return hasAccess;
	}

	@Override
	boolean process(Player player, String... params) {
		if (!validateAccess(player))
			return player.isStaff(); // return false for regular players, so chat will send entered text (this way you can't guess commands without rights)

		if (!run(player, params))
			sendInfo(player, "<Error while executing command>");

		return true;
	}
}
