package com.aionemu.gameserver.utils.chathandlers;

import java.awt.Color;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.administration.CommandsConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.L10n;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
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
	private final String syntaxInfo;

	/**
	 * Initializes a chat command.
	 * 
	 * @param prefix
	 *          prefix for this command
	 * @param alias
	 *          command identifier
	 * @param description
	 *          short command description
	 * @param syntaxInfo
	 *          The command parameter info. It is used to generate the syntax info in {@link #sendInfo(Player, String...)}.<br>
	 *          When following the parameter convention, parameters will be highlighted in white. You can pass a text block if your command supports
	 *          multiple syntax variants.<br>
	 *          Example:
	 *          <pre>{@code
	 *          """
	 *          - Short description for no parameter.
	 *          <param1> <param2> [optionalParam3] - Short parameter description (two mandatory parameters, third one is optional).
	 *          param1 <param2> - Short parameter description (first one is a non-variable word).
	 *          Some other help text.
	 *          """
	 *          }</pre>
	 */
	public ChatCommand(String prefix, String alias, String description, String syntaxInfo) {
		this.prefix = prefix;
		this.alias = alias;
		this.description = description;
		this.syntaxInfo = parseSyntaxInfo(syntaxInfo);
	}

	public final boolean run(Player player, String... params) {
		if (params.length == 1 && "help".equalsIgnoreCase(params[0])) {
			sendInfo(player, "Command: " + ChatUtil.color(getAliasWithPrefix(), Color.WHITE) + "\n\t"
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

	protected String getAliasForLevel() {
		return alias;
	}

	public final String getDescription() {
		return description;
	}

	public final String getAliasWithPrefix() {
		return prefix + alias;
	}

	public String getSyntaxInfo() {
		return syntaxInfo;
	}

	private String parseSyntaxInfo(String syntaxInfo) {
		StringBuilder sb = new StringBuilder();
		sb.append("Syntax:");
		if (syntaxInfo.isBlank()) {
			sb.append("\n\tNo syntax info available.");
		} else {
			boolean containsSquareBrackets = false;
			for (String info : syntaxInfo.split("\n")) {
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
		}
		return sb.toString();
	}

	public final byte getLevel() {
		Byte level = CommandsConfig.ACCESS_LEVELS.get(getAliasForLevel());
		if (level == null)
			throw new NullPointerException("Missing access level for " + prefix + getAliasForLevel());
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
			try {
				Class<?> enumClass = Class.forName(Stream.of(enumParts).limit(enumParts.length - 1).collect(Collectors.joining(".")));
				@SuppressWarnings("unchecked")
				String values = Stream.of(((Class<Enum<?>>) enumClass).getEnumConstants()).map(Enum::toString).collect(Collectors.joining(", "));
				msg += "\nPossible values:\n" + values;
			} catch (Exception ex) {
				log.error("Could not get enum values for " + enumName, ex);
			}
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
	 * If no message parameter (or <tt>null</tt>) is specified, the default syntax info will be sent.
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
		ChatUtil.split(sb.toString()).forEach(part -> PacketSendUtility.sendMessage(player, part));
	}

	protected static String join(String[] params, int startIndex) {
		return Stream.of(params).skip(startIndex).collect(Collectors.joining(" "));
	}

	/**
	 * @return The name of the object to be displayed in chat. If the object is a player, a clickable name will be returned. Otherwise, it's a localized name if available.
	 */
	protected static String name(VisibleObject visibleObject) {
		if (visibleObject instanceof Player player)
			return ChatUtil.charName(player);
		if (visibleObject.getObjectTemplate() instanceof L10n l10n)
			return l10n.getL10n();
		return visibleObject.getName();
	}

	/**
	 * @return The name of the world to be displayed in chat. If available, returns its localized name.
	 */
	protected static String worldName(int worldId) {
		WorldMapTemplate template = DataManager.WORLD_MAPS_DATA.getTemplate(worldId);
		if (template == null)
			return String.valueOf(worldId);
		return template.getL10nId() != 0 ? template.getL10n() : template.getName();
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
