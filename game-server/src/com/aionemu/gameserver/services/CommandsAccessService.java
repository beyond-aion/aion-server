package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.CommandsAccessDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;

/**
 * @author ViAl
 */
public class CommandsAccessService {

	private Map<Integer, List<String>> playersAccess;

	private CommandsAccessService() {
		this.playersAccess = DAOManager.getDAO(CommandsAccessDAO.class).loadAccesses();
	}

	public static final CommandsAccessService getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final CommandsAccessService instance = new CommandsAccessService();
	}

	public void giveAccess(Player admin, int playerId, String command) {
		if (hasAccess(playerId, command)) {
			PacketSendUtility.sendMessage(admin, "This player already has access on command " + command);
			return;
		}
		if (!ChatProcessor.getInstance().isCommandExists(command)) {
			PacketSendUtility.sendMessage(admin, "There is no such admin command as \"" + command + "\"");
			return;
		}
		if (!playersAccess.containsKey(playerId)) {
			playersAccess.put(playerId, new ArrayList<String>());
		}
		List<String> commands = playersAccess.get(playerId);
		commands.add(command);
		DAOManager.getDAO(CommandsAccessDAO.class).addAccess(playerId, command);
		PacketSendUtility.sendMessage(admin, "Command access was granted successfuly.");
	}

	public void removeAccess(Player admin, int playerId, String command) {
		if (!hasAccess(playerId, command)) {
			PacketSendUtility.sendMessage(admin, "This player has no access on command " + command);
			return;
		}
		List<String> commands = playersAccess.get(playerId);
		commands.remove(command);
		DAOManager.getDAO(CommandsAccessDAO.class).removeAccess(playerId, command);
		PacketSendUtility.sendMessage(admin, "Command access was removed successfully");
	}

	public void removeAllAccesses(int playerId) {
		if (this.playersAccess.containsKey(playerId)) {
			List<String> commands = this.playersAccess.get(playerId);
			commands.clear();
			DAOManager.getDAO(CommandsAccessDAO.class).removeAllAccesses(playerId);
		}
	}

	public boolean hasAccess(int playerId, String command) {
		if (!playersAccess.containsKey(playerId))
			return false;
		List<String> commands = playersAccess.get(playerId);
		if (commands.contains(command))
			return true;
		else
			return false;
	}
}
