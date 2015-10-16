package com.aionemu.gameserver.utils.chathandlers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javolution.util.FastMap;

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

/**
 * @author KID
 * @modified Rolandas, Neon
 */
public class ChatProcessor implements GameEngine {

	private static final Logger log = LoggerFactory.getLogger(ChatProcessor.class);
	private static int commandsBefore;
	private static ChatProcessor instance = new ChatProcessor();
	private Map<String, ChatCommand> commands = new FastMap<String, ChatCommand>();
	private Map<String, Byte> accessLevel = new FastMap<String, Byte>();
	private ScriptManager sm = new ScriptManager();
	private Exception loadException = null;

	public static ChatProcessor getInstance() {
		return instance;
	}

	@Override
	public void load(CountDownLatch progressLatch) {
		try {
			log.info("Chat processor load started");
			init(sm, this);
		} finally {
			if (progressLatch != null)
				progressLatch.countDown();
		}
	}

	@Override
	public void shutdown() {
	}

	private ChatProcessor() {
	}

	private ChatProcessor(ScriptManager scriptManager) {
		init(scriptManager, this);
	}

	private void init(final ScriptManager scriptManager, ChatProcessor processor) {
		commandsBefore = commands.size();
		loadLevels();

		AggregatedClassListener acl = new AggregatedClassListener();
		acl.addClassListener(new OnClassLoadUnloadListener());
		acl.addClassListener(new ScheduledTaskClassListener());
		acl.addClassListener(new ChatCommandsLoader(processor));
		scriptManager.setGlobalClassListener(acl);

		final File[] files = new File[] { new File("./data/scripts/system/adminhandlers.xml"), new File("./data/scripts/system/playerhandlers.xml"),
			new File("./data/scripts/system/consolehandlers.xml") };
		final CountDownLatch loadLatch = new CountDownLatch(files.length);

		for (int i = 0; i < files.length; i++) {
			final int index = i;
			ThreadPoolManager.getInstance().execute(new Runnable() {

				@Override
				public void run() {
					try {
						scriptManager.load(files[index]);
					} catch (Exception e) {
						loadException = e;
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
		if (loadException != null) {
			throw new GameServerError("Can't initialize chat handlers.", loadException);
		}
	}

	public void registerCommand(ChatCommand cmd) {
		if (commands.containsKey(cmd.getAlias())) {
			log.warn("Failed to register chat command: " + cmd.getAlias() + " is already registered.");
			return;
		}

		if (!accessLevel.containsKey(cmd.getAlias())) {
			log.warn("Failed to register chat command: Missing access level for " + cmd.getAlias() + ".");
			return;
		}

		cmd.setAccessLevel(accessLevel.get(cmd.getAlias()));
		commands.put(cmd.getAlias(), cmd);
	}

	public void reload() {
		ScriptManager tmpSM;
		final ChatProcessor adminCP;
		Map<String, ChatCommand> backupCommands = new FastMap<String, ChatCommand>();
		backupCommands.putAll(commands);
		commands.clear();
		loadException = null;

		try {
			tmpSM = new ScriptManager();
			adminCP = new ChatProcessor(tmpSM);
		} catch (Throwable e) {
			commands = backupCommands;
			throw new GameServerError("Can't reload chat handlers.", e);
		}

		if (tmpSM != null && adminCP != null) {
			backupCommands.clear();
			sm.shutdown();
			sm = null;
			sm = tmpSM;
			instance = adminCP;
		}
	}

	private void loadLevels() {
		accessLevel.clear();
		try {
			java.util.Properties props = PropertiesUtils.load("config/administration/commands.properties");

			for (Object key : props.keySet()) {
				String str = (String) key;
				accessLevel.put(str, Byte.valueOf(props.getProperty(str).trim()));
			}
		} catch (IOException e) {
			log.error("Can't read commands.properties", e);
		}
	}

	public boolean handleChatCommand(Player player, String text) {
		if (text == null || text.isEmpty())
			return false;

		if (!StringUtils.startsWithAny(text, AdminCommand.PREFIX, PlayerCommand.PREFIX))
			return false;

		String cmdName = text.split(" ")[0];
		String cmdParams = text.substring(cmdName.length());
		for (ChatCommand cmd : getCommandList()) {
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
			return new String[] {};

		// advanced split to keep item links etc. in one piece (splitting on spaces, but only outside of square brackets)
		return params.trim().split(" +(?=[^\\]]*(\\[|$))");
	}

	private ChatCommand getCommand(String alias) {
		return this.commands.get(alias);
	}

	public Collection<ChatCommand> getCommandList() {
		return Collections.unmodifiableCollection(this.commands.values());
	}

	public boolean isCommandAllowed(Player executor, String alias) {
		return isCommandAllowed(executor, getCommand(alias));
	}

	public boolean isCommandAllowed(Player executor, ChatCommand command) {
		return command != null && command.validateAccess(executor);
	}

	public boolean isCommandExists(String alias) {
		return this.commands.containsKey(alias);
	}

	public void onCompileDone() {
		log.info("Loaded " + (commands.size() - commandsBefore) + " commands.");
		commandsBefore = commands.size();
	}
}
