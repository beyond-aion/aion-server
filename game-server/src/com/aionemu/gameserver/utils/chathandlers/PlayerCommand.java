package com.aionemu.gameserver.utils.chathandlers;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.CommandsAccessService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author synchro2
 */
public abstract class PlayerCommand extends ChatCommand {

	public PlayerCommand(String alias) {
		super(alias);
	}

	@Override
	public boolean checkLevel(Player player) {
		return player.havePermission(getLevel()) || CommandsAccessService.getInstance().haveRigths(player.getObjectId(), getAlias());
	}

	@Override
	boolean process(Player player, String text) {
		if (!checkLevel(player)) {
			PacketSendUtility.sendMessage(player, "You not have permission for use this command.");
			return true;
		}

		boolean success = false;
		if (text.length() == getAlias().length())
			success = this.run(player, EMPTY_PARAMS);
		else
			success = this.run(player, text.substring(getAlias().length() + 1).split(" "));

		return success;
	}
}
