package com.aionemu.gameserver.utils.chathandlers;

import java.awt.Color;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author KID
 * @modified Neon
 */
public abstract class ChatCommand {

	private static final Logger log = LoggerFactory.getLogger(ChatCommand.class);
	private final String prefix;
	private final String alias;
	private final String description;
	private String syntaxInfo;
	private byte level;

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
			execute(player, params);
		} catch (Exception e) {
			log.error("Exception executing chat command " + getAliasWithPrefix() + " " + String.join(" ", params) + "\nPlayer: " + player.getName()
				+ "\nTarget: " + player.getTarget(), e);
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
	 * You can pass multiple comma separated <b>syntaxInfo</b> strings. Each string will result in a line of text output.
	 * 
	 * @param syntaxInfo
	 *          strings should look like this:<br>
	 *          &nbsp;&nbsp;" - Short description for no parameter.",<br>
	 *          &nbsp;&nbsp;"&lt;param1a|param1b&gt; - Short parameter description.",<br>
	 *          &nbsp;&nbsp;"&lt;param1&gt; &lt;param2&gt; [optional param3] - Short parameter description."
	 */
	protected final void setSyntaxInfo(String... lines) {
		this.syntaxInfo = parseSyntaxInfo(lines);
	}

	private String getSyntaxInfo() {
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
				if (StringUtils.startsWithAny(info, " ", "<", "[")) {
					if (!containsSquareBrackets && info.contains("["))
						containsSquareBrackets = true;
					sb.append("\n\t").append(ChatUtil.color(getAliasWithPrefix(), Color.WHITE)).append(' ');
					String[] split = info.split(" - ", 2);
					if (split.length == 2) {
						sb.append(split[0].replaceAll("([^<>\\[\\]| ]+)", ChatUtil.color("$1", Color.WHITE)).trim());
						sb.append(" - ");
						sb.append(split[1]);
					} else {
						sb.append(info);
					}
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

	public final void setAccessLevel(byte level) {
		this.level = level;
	}

	public final byte getLevel() {
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
	 * The code to be executed after successful command access validation.
	 * 
	 * @param player
	 * @param params
	 */
	protected abstract void execute(Player player, String... params);

	/**
	 * Sends an info message to the player.<br>
	 * If no message parameter is specified, the default syntax info will be sent.<br>
	 * You can set syntax info via {@link #setSyntaxInfo(String...)}
	 * 
	 * @param player
	 *          player who will receive the message
	 * @param message
	 *          message text (insert newlines with \n or by passing comma separated strings)
	 */
	protected final void sendInfo(Player player, String... message) {
		StringBuilder sb = new StringBuilder();
		if (message.length > 0) {
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
					PacketSendUtility.sendMessage(player, sb.toString());
					sb.setLength(0);
				} else {
					sb.append('\n');
				}
				sb.append(lines[i]);
			}
			PacketSendUtility.sendMessage(player, sb.toString());
		}
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
