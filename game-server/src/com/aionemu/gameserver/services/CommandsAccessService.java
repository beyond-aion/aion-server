package com.aionemu.gameserver.services;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.aionemu.gameserver.dao.CommandsAccessDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;

/**
 * @author ViAl, Neon
 */
public class CommandsAccessService {

	private static Map<Integer, Set<String>> commandAccesses = Collections.emptyMap();

	private CommandsAccessService() {
	}

	public static void loadAccesses() {
		commandAccesses = CommandsAccessDAO.loadAccesses();
	}

	public static void giveTemporaryAccess(Player admin, int playerId, String command) {
		giveAccess(admin, playerId, command, true);
	}

	public static void giveAccess(Player admin, int playerId, String command) {
		giveAccess(admin, playerId, command, false);
	}

	private static void giveAccess(Player admin, int playerId, String command, boolean isTemporary) {
		if (hasAccess(playerId, command)) {
			PacketSendUtility.sendMessage(admin, "This player already has access on command " + command);
			return;
		}
		if (!ChatProcessor.getInstance().isCommandExists(command)) {
			PacketSendUtility.sendMessage(admin, "There is no such admin command as \"" + command + "\"");
			return;
		}
		commandAccesses.compute(playerId, (key, commands) -> {
			if (commands == null)
				commands = new HashSet<>();
			commands.add(command);
			return commands;
		});
		if (!isTemporary)
			CommandsAccessDAO.addAccess(playerId, command);
		PacketSendUtility.sendMessage(admin, "Command access was granted successfuly.");
	}

	public static void removeAccess(Player admin, int playerId, String command) {
		if (!hasAccess(playerId, command)) {
			PacketSendUtility.sendMessage(admin, "This player has no access on command " + command);
			return;
		}
		Set<String> commands = commandAccesses.get(playerId);
		commands.remove(command);
		CommandsAccessDAO.removeAccess(playerId, command);
		PacketSendUtility.sendMessage(admin, "Command access was removed successfully");
	}

	public static boolean removeAllAccesses(int playerId) {
		Set<String> commands = commandAccesses.get(playerId);
		if (commands != null) {
			commands.clear();
			CommandsAccessDAO.removeAllAccesses(playerId);
			return true;
		}
		return false;
	}

	public static boolean hasAccess(int playerId, String command) {
		Set<String> commands = commandAccesses.get(playerId);
		return commands != null && commands.contains(command);
	}

}
