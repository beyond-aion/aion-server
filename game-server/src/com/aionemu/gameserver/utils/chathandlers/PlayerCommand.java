package com.aionemu.gameserver.utils.chathandlers;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.CommandsAccessService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author synchro2
 * @modified Neon
 */
public abstract class PlayerCommand extends ChatCommand {

	public final static String PREFIX = ".";

	public PlayerCommand(String alias) {
		this(alias, "");
	}

	public PlayerCommand(String alias, String description) {
		super(alias, description, PREFIX);
	}

	@Override
	public boolean checkLevel(Player player) {
		return player.havePermission(getLevel()) || CommandsAccessService.getInstance().haveRigths(player.getObjectId(), getAlias());
	}

	@Override
	boolean process(Player player, String params) {

		if (!checkLevel(player)) {
			PacketSendUtility.sendMessage(player, "You don't have permission to use this command.");
			return true;
		}

		boolean success = false;
		if (params.isEmpty())
			success = this.run(player, EMPTY_PARAMS);
		else
			success = this.run(player, params.split(" "));

		return success;
	}
}
