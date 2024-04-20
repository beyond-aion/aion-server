package com.aionemu.gameserver.utils.chathandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.ScriptManager;
import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.configs.Config;
import com.aionemu.gameserver.configs.administration.CommandsConfig;
import com.aionemu.gameserver.model.GameEngine;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author KID, Rolandas, Neon
 */
public class ChatProcessor implements GameEngine {

	private static final Logger log = LoggerFactory.getLogger(ChatProcessor.class);
	private final Map<String, ChatCommand> commandHandlers = new HashMap<>();

	private ChatProcessor() {
	}

	@Override
	public void load() {
		log.info("Chat processor load started");

		ScriptManager scriptManager = new ScriptManager();
		scriptManager.setGlobalClassListener(new ChatCommandsLoader(this));

		try {
			scriptManager.load(CommandsConfig.HANDLER_DIRECTORIES);
		} catch (Exception e) {
			throw new GameServerError("Can't initialize chat handlers.", e);
		} finally {
			scriptManager.shutdown();
		}

		log.info("Loaded " + commandHandlers.size() + " commands.");
	}

	public void reload() {
		Map<String, ChatCommand> oldCommands = new HashMap<>(commandHandlers);
		try {
			Config.load(CommandsConfig.class);
			commandHandlers.clear();
			load();
		} catch (Throwable e) {
			commandHandlers.clear();
			commandHandlers.putAll(oldCommands);
			throw e;
		}
	}

	@Override
	public void shutdown() {
	}

	public void registerCommand(ChatCommand cmd) {
		if (commandHandlers.containsKey(cmd.getAlias()))
			throw new IllegalArgumentException("Failed to register chat command: " + cmd.getAlias() + " is already registered.");

		if (cmd.getLevel() < 0)
			throw new NullPointerException("Failed to register chat command: Invalid access level for " + cmd.getAlias() + ".");

		commandHandlers.put(cmd.getAlias(), cmd);
	}

	public boolean handleChatCommand(Player player, String text) {
		if (text == null || text.isEmpty())
			return false;

		if (!StringUtils.startsWithAny(text, AdminCommand.PREFIX, PlayerCommand.PREFIX))
			return false;

		String cmdName = text.split(" ")[0];
		String cmdParams = text.substring(cmdName.length());
		for (ChatCommand cmd : commandHandlers.values()) {
			if (!(cmd instanceof ConsoleCommand) && cmdName.equals(cmd.getAliasWithPrefix()))
				return cmd.process(player, getParamsFromString(cmdParams));
		}

		return false;
	}

	public void handleConsoleCommand(Player player, String text) {
		if (text == null || text.isEmpty())
			return;

		if (!text.startsWith(ConsoleCommand.PREFIX))
			return;

		String cmdName = text.split(" ")[0];
		String cmdParams = text.substring(cmdName.length());

		// TODO remove this temporary fix (AdminCommand is already called addskill)
		if (cmdName.endsWith("addskill"))
			cmdName = cmdName.replace("addskill", "addcskill");

		ChatCommand cmd = getCommand(cmdName.substring(ConsoleCommand.PREFIX.length()));

		if (cmd == null)
			PacketSendUtility.sendMessage(player, "The command " + cmdName + " is not implemented.");
		else if (cmd instanceof ConsoleCommand)
			cmd.process(player, getParamsFromString(cmdParams));
	}

	private String[] getParamsFromString(String params) {
		if (params == null || params.trim().isEmpty())
			return new String[0];

		// advanced split to keep item links etc. in one piece (splitting on spaces, but only outside of square brackets)
		return params.trim().split(" +(?=[^\\]]*(\\[|$))");
	}

	private ChatCommand getCommand(String alias) {
		return commandHandlers.get(alias);
	}

	/**
	 * @return Command of the given type. It will not find a command if you call this method from any non-chatcommand class.
	 *         This is because of different class loaders. You can still get a command from a core class via {@link #getCommand(String)} but won't be
	 *         able to cast it to it's runtime type (supertypes like {@link ChatCommand} however will work).
	 */
	@SuppressWarnings("unchecked")
	public <T extends ChatCommand> T getCommand(Class<T> commandType) {
		return (T) commandHandlers.values().stream().filter(c -> commandType == c.getClass()).findAny().orElse(null);
	}

	public List<ChatCommand> getCommandList() {
		return new ArrayList<>(commandHandlers.values());
	}

	public boolean isCommandAllowed(Player executor, String alias) {
		return isCommandAllowed(executor, getCommand(alias));
	}

	public boolean isCommandAllowed(Player executor, ChatCommand command) {
		return command != null && command.validateAccess(executor);
	}

	public boolean isCommandExists(String alias) {
		return commandHandlers.containsKey(alias);
	}

	public static ChatProcessor getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final ChatProcessor instance = new ChatProcessor();
	}
}
