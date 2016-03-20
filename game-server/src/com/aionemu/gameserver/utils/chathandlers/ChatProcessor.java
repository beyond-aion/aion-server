package com.aionemu.gameserver.utils.chathandlers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.classlistener.AggregatedClassListener;
import com.aionemu.commons.scripting.classlistener.OnClassLoadUnloadListener;
import com.aionemu.commons.scripting.classlistener.ScheduledTaskClassListener;
import com.aionemu.commons.scripting.scriptmanager.ScriptManager;
import com.aionemu.commons.utils.PropertiesUtils;
import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.model.GameEngine;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import javolution.util.FastMap;
import javolution.util.FastTable;

/**
 * @author KID
 * @modified Rolandas, Neon
 */
public class ChatProcessor implements GameEngine {

	private static final Logger log = LoggerFactory.getLogger(ChatProcessor.class);
	private ScriptManager scriptManager = new ScriptManager();
	private Map<String, ChatCommand> commandHandlers = new FastMap<>();
	private Map<String, Byte> accessLevel = new FastMap<>();

	private ChatProcessor() {
	}

	@Override
	public void load(CountDownLatch progressLatch) {
		log.info("Chat processor load started");

		try {
			try {
				Properties props = PropertiesUtils.load("config/administration/commands.properties");

				for (Object key : props.keySet()) {
					String str = (String) key;
					accessLevel.put(str, Byte.valueOf(props.getProperty(str).trim()));
				}
			} catch (IOException e) {
				log.error("Can't read commands.properties", e);
			}

			AggregatedClassListener acl = new AggregatedClassListener();
			acl.addClassListener(new OnClassLoadUnloadListener());
			acl.addClassListener(new ScheduledTaskClassListener());
			acl.addClassListener(new ChatCommandsLoader(this));
			scriptManager.setGlobalClassListener(acl);

			File[] files = new File[] { new File("./data/scripts/system/adminhandlers.xml"), new File("./data/scripts/system/playerhandlers.xml"),
				new File("./data/scripts/system/consolehandlers.xml") };
			CountDownLatch loadLatch = new CountDownLatch(files.length);
			Throwable[] throwable = new Throwable[1];

			for (File file : files) {
				ThreadPoolManager.getInstance().execute(new Runnable() {

					@Override
					public void run() {
						try {
							scriptManager.load(file);
						} catch (Throwable t) {
							throwable[0] = t;
						} finally {
							loadLatch.countDown();
						}
					}
				});
			}

			try {
				loadLatch.await();
			} catch (InterruptedException e1) {
			}

			if (throwable[0] != null)
				throw new GameServerError("Can't initialize chat handlers.", throwable[0]);

			log.info("Loaded " + commandHandlers.size() + " commands.");
		} finally {
			if (progressLatch != null)
				progressLatch.countDown();
		}
	}

	public void reload() {
		Map<String, Byte> backupAccessLevels = FastMap.of(accessLevel);
		Map<String, ChatCommand> backupCommands = FastMap.of(commandHandlers);
		shutdown();

		try {
			load(null);
		} catch (Throwable e) {
			accessLevel = backupAccessLevels;
			commandHandlers = backupCommands;
			log.warn("Can't reload chat handlers, restored previously loaded commands.", e);
		}
	}

	@Override
	public void shutdown() {
		log.info("Chat processor shutdown started");
		scriptManager.shutdown();
		accessLevel.clear();
		commandHandlers.clear();
		log.info("Chat processor shutdown complete");
	}

	public void registerCommand(ChatCommand cmd) {
		if (commandHandlers.containsKey(cmd.getAlias())) {
			log.warn("Failed to register chat command: " + cmd.getAlias() + " is already registered.");
			return;
		}

		if (!accessLevel.containsKey(cmd.getAlias())) {
			log.warn("Failed to register chat command: Missing access level for " + cmd.getAlias() + ".");
			return;
		}

		cmd.setAccessLevel(accessLevel.get(cmd.getAlias()));
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

		if (cmd instanceof ConsoleCommand)
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

	public Collection<ChatCommand> getCommandList() {
		return FastTable.of(commandHandlers.values());
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

	public static final ChatProcessor getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final ChatProcessor instance = new ChatProcessor();
	}
}
