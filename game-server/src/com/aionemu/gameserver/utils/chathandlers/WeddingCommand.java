package com.aionemu.gameserver.utils.chathandlers;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author synchro2
 */
public abstract class WeddingCommand extends ChatCommand {

	public WeddingCommand(String alias) {
		super(alias);
	}

	@Override
	public boolean checkLevel(Player player) {
		return player.havePermission(getLevel());
	}

	@Override
	boolean process(Player player, String text) {
		if (!player.isMarried())
			return false;
		String alias = this.getAlias();

		if (!checkLevel(player)) {
			PacketSendUtility.sendMessage(player, "You not have permission for use this command.");
			return true;
		}

		boolean success = false;
		if (text.length() == alias.length())
			success = this.run(player, EMPTY_PARAMS);
		else
			success = this.run(player, text.substring(alias.length() + 1).split(" "));

		return success;
	}
}
