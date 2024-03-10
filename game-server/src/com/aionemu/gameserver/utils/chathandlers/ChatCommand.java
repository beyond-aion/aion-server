package com.aionemu.gameserver.utils.chathandlers;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.administration.CommandsConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author KID, Neon
 */
public abstract class ChatCommand {

	private static final Logger log = LoggerFactory.getLogger(ChatCommand.class);
	private final String prefix;
	private final String alias;
	private final String description;
	private String syntaxInfo;

	/**
	 * Initializes a chat command.
	 * 
	 * @param prefix
	 *          prefix for this command
	 * @param alias
	 *          command identifier
	 * @param description
	 *          short command description
	 */
	public ChatCommand(String prefix, String alias, String description) {
		this.prefix = prefix;
		this.alias = alias;
		this.description = description;
	}

	public final boolean run(Player player, String... params) {
		if (params.length == 1 && "help".equalsIgnoreCase(params[0])) {
			sendMessagePackets(player, "Command: " + ChatUtil.color(getAliasWithPrefix(), Color.WHITE) + "\n\t"
				+ (getDescription().isEmpty() ? "No description available." : getDescription()) + "\n" + getSyntaxInfo());
			return true;
		}

		try {
			try {
				execute(player, params);
			} catch (IllegalArgumentException e) {
				sendInfo(player, toErrorMessage(e));
			}
		} catch (Throwable t) {
			log.error("Exception executing chat command \"" + getAliasWithPrefix() + " " + String.join(" ", params) + "\" - Player: " + player.getName()
				+ ", Target: " + player.getTarget(), t);
			return false;
		}
		return true;
	}

	public final String getPrefix() {
		return prefix;
	}

	public final String getAlias() {
		return alias;
	}

	public final String getDescription() {
		return description;
	}

	public final String getAliasWithPrefix() {
		return prefix + alias;
	}

	/**
	 * Sets the command parameter info.<br>
	 * This parameter info is needed to generate the syntax info in {@link #sendInfo(Player, String...)}.<br>
	 * You can pass multiple comma separated lines of text. When following the parameter convention, parameters will be highlighted in white.
	 * 
	 * @param lines
	 *          strings may look like this:<br>
	 *          " - Short description for no parameter.",<br>
	 *          "&lt;param1&gt; &lt;param2&gt; [optionalParam3] - Short parameter description (two mandatory parameters, third one is optional).",<br>
	 *          "param1 &lt;param2&gt; - Short parameter description (first one is a non-variable word).",<br>
	 *          "Some other help text."<br>
	 */
	protected final void setSyntaxInfo(String... lines) {
		this.syntaxInfo = parseSyntaxInfo(lines);
	}

	public String getSyntaxInfo() {
		if (syntaxInfo == null) // init default info if handler did not set any syntax info
			setSyntaxInfo();
		return syntaxInfo;
	}

	private String parseSyntaxInfo(String... lines) {
		StringBuilder sb = new StringBuilder();
		sb.append("Syntax:");
		if (lines.length > 0) {
			boolean containsSquareBrackets = false;
			for (String info : lines) {
				String[] split = info.split(" - ", 2);
				if (split.length == 2) {
					if (!containsSquareBrackets && split[0].contains("["))
						containsSquareBrackets = true;
					sb.append("\n\t").append(ChatUtil.color(getAliasWithPrefix(), Color.WHITE)).append(' ');
					sb.append(split[0].replaceAll("([^<>\\[\\]| ]+)", ChatUtil.color("$1", Color.WHITE)).replace("[[color:f;", "[[color:f\u200B;").trim());
					sb.append(" - ");
					sb.append(split[1]);
				} else {
					sb.append("\n").append(info);
				}
			}
			if (containsSquareBrackets)
				sb.append("\nNote: Parameters enclosed in square brackets are optional.");
		} else {
			sb.append("\n\tNo syntax info available.");
		}
		return sb.toString();
	}

	public final byte getLevel() {
		Byte level = CommandsConfig.ACCESS_LEVELS.get(alias);
		if (level == null)
			throw new NullPointerException("Missing access level for " + getAliasWithPrefix());
		return level;
	}

	/**
	 * @param player
	 * @return True if player is allowed to use this command.
	 */
	abstract boolean validateAccess(Player player);

	/**
	 * Handles processing of a chat command.
	 * 
	 * @param player
	 * @param params
	 * @return True if command was executed.
	 */
	abstract boolean process(Player player, String... params);

	/**
	 * The code to be executed after successful command access validation. Any IllegalArgumentException and its subclasses will be catched, printing the
	 * error message to the player (using {@link #toErrorMessage(IllegalArgumentException)}).
	 */
	protected abstract void execute(Player player, String... params);

	/**
	 * This method can be overridden in case the default message extraction is not sufficient.
	 * 
	 * @return Message that should be sent to the player who caused the exception with invalid input. If null, the default syntax info will be sent, as
	 *         specified by {@link #sendInfo(Player, String...)}
	 */
	protected String toErrorMessage(IllegalArgumentException e) {
		String msg = e.getMessage();
		if (msg != null && msg.startsWith("No enum constant ")) { // "No enum constant com.aionemu.gameserver.model.siege.SiegeRace.invalidName"
			String[] enumParts = msg.substring(17).split("\\."); // -> ["com", "aionemu", "gameserver", "model", "siege", "SiegeRace", "invalidName"]
			String enumName = enumParts[enumParts.length - 2]; // -> "SiegeRace"
			// split camelCase word (https://stackoverflow.com/a/7599674)
			String[] enumNameParts = enumName.split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"); // -> ["Siege", "Race"]
			enumName = String.join(" ", enumNameParts).toLowerCase(); // -> "siege race"
			msg = "Invalid " + enumName + ".";
		} else if (e instanceof NumberFormatException) { // Integer.parseInt and Long.parseLong don't provide nice error messages
			if (msg != null && msg.startsWith("For input string: "))
				msg = "Invalid number: " + msg.substring(18);
			else
				msg = "Invalid number.";
		}
		return msg;
	}

	/**
	 * Sends an info message to the player.<br>
	 * If no message parameter (or <tt>null</tt>) is specified, the default syntax info will be sent.<br>
	 * You can set syntax info via {@link #setSyntaxInfo(String...)}
	 * 
	 * @param player
	 *          player who will receive the message
	 * @param message
	 *          message text (insert newlines with \n or by passing comma separated strings)
	 */
	protected final void sendInfo(Player player, String... message) {
		StringBuilder sb = new StringBuilder();
		if (message.length > 1 || message.length == 1 && message[0] != null) {
			for (int i = 0; i < message.length; i++) {
				if (i > 0)
					sb.append('\n');
				sb.append(message[i]);
			}
		} else {
			sb.append(getSyntaxInfo());
		}
		sendMessagePackets(player, sb.toString());
	}

	/**
	 * Sends the formatted input message with as little packets as possible.
	 * 
	 * @param player
	 * @param message
	 */
	private static void sendMessagePackets(Player player, String message) {
		int lineLimit = 15; // length limit check alone is not safe if you send chat links (they can exceeded the display limit on client side)
		String[] lines = message.split("\n", -1);
		if (message.length() <= SM_MESSAGE.MESSAGE_SIZE_LIMIT && lines.length <= lineLimit) {
			PacketSendUtility.sendMessage(player, message);
		} else {
			StringBuilder sb = new StringBuilder(lines[0]);
			for (int i = 1; i < lines.length; i++) {
				if (i % lineLimit == 0 || sb.length() + 1 + lines[i].length() > SM_MESSAGE.MESSAGE_SIZE_LIMIT) { // current length + newLine char + next line length
					sendSafe(player, sb.toString());
					sb.setLength(0);
				} else {
					sb.append('\n');
				}
				sb.append(lines[i]);
			}
			sendSafe(player, sb.toString());
		}
	}

	/**
	 * Divides up the (single line) message if necessary and sends multiple packets (lines) to ensure to stay within the character limit per line.
	 */
	private static void sendSafe(Player player, String msg) {
		if (msg.length() > SM_MESSAGE.MESSAGE_SIZE_LIMIT) {
			int splitIndex = findSplitIndex(msg, ',', ']', ' ') + 1;
			PacketSendUtility.sendMessage(player, msg.substring(0, splitIndex));
			sendSafe(player, msg.substring(splitIndex));
		} else {
			PacketSendUtility.sendMessage(player, msg);
		}
	}

	private static int findSplitIndex(String msg, char... splitChars) {
		int searchStartIndex = Math.min(msg.length() / 2, SM_MESSAGE.MESSAGE_SIZE_LIMIT / 2);
		for (char splitChar : splitChars) {
			int splitIndex = msg.indexOf(splitChar, searchStartIndex);
			if (splitIndex > -1 && splitIndex <= SM_MESSAGE.MESSAGE_SIZE_LIMIT)
				return splitIndex;
		}
		return SM_MESSAGE.MESSAGE_SIZE_LIMIT;
	}

	/**
	 * Please use {@link #sendInfo(Player, String...)}.
	 * Old commands still override this method to show syntax info and should be ported eventually.
	 * TODO: remove this method when all commands are updated
	 */
	@Deprecated
	protected void info(Player player, String message) {
		throw new UnsupportedOperationException("Please don't call me and don't override me! Use sendInfo() instead. Syntax info can be initialized in constructor.");
	}
}
