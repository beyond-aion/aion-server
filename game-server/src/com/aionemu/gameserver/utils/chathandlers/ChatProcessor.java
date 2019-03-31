package com.aionemu.gameserver.utils.chathandlers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.classlistener.AggregatedClassListener;
import com.aionemu.commons.scripting.classlistener.OnClassLoadUnloadListener;
import com.aionemu.commons.scripting.classlistener.ScheduledTaskClassListener;
import com.aionemu.commons.scripting.scriptmanager.ScriptManager;
import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.configs.Config;
import com.aionemu.gameserver.configs.administration.CommandsConfig;
import com.aionemu.gameserver.model.GameEngine;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author KID
 * @modified Rolandas, Neon
 */
public class ChatProcessor implements GameEngine {

	private static final Logger log = LoggerFactory.getLogger(ChatProcessor.class);
	private final Map<String, ChatCommand> commandHandlers = new HashMap<>();

	private ChatProcessor() {
	}

	@Override
	public void load() {
		log.info("Chat processor load started");

		AggregatedClassListener acl = new AggregatedClassListener();
		acl.addClassListener(new OnClassLoadUnloadListener());
		acl.addClassListener(new ScheduledTaskClassListener());
		acl.addClassListener(new ChatCommandsLoader(this));
		ScriptManager scriptManager = new ScriptManager();
		scriptManager.setGlobalClassListener(acl);

		File[] files = new File[] { new File("./data/scripts/system/adminhandlers.xml"), new File("./data/scripts/system/playerhandlers.xml"),
			new File("./data/scripts/system/consolehandlers.xml") };
		CountDownLatch loadLatch = new CountDownLatch(files.length);
		Throwable[] throwable = new Throwable[1];

		for (File file : files) {
			ThreadPoolManager.getInstance().execute(() -> {
				try {
					scriptManager.load(file);
				} catch (Throwable t) {
					throwable[0] = t;
				} finally {
					loadLatch.countDown();
				}
			});
		}

		try {
			loadLatch.await();
		} catch (InterruptedException e1) {
		} finally {
			scriptManager.shutdown();
		}

		if (throwable[0] != null)
			throw new GameServerError("Can't initialize chat handlers.", throwable[0]);

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
