package com.aionemu.gameserver.configs.main;

import java.io.File;
import java.time.ZoneId;

import com.aionemu.commons.configuration.Property;

public class GSConfig {

	/**
	 * Gameserver
	 */

	/* Server Country Code */
	@Property(key = "gameserver.country.code", defaultValue = "99")
	public static int SERVER_COUNTRY_CODE;

	/* Players Max Level */
	@Property(key = "gameserver.players.max.level", defaultValue = "65")
	public static int PLAYER_MAX_LEVEL;

	/* Time Zone */
	@Property(key = "gameserver.timezone")
	public static ZoneId TIME_ZONE_ID;

	/* Enable connection with CS (ChatServer) */
	@Property(key = "gameserver.chatserver.enable", defaultValue = "false")
	public static boolean ENABLE_CHAT_SERVER;

	/** Min. required level to write in CS channels */
	@Property(key = "gameserver.chatserver.min_level", defaultValue = "10")
	public static byte CHAT_SERVER_MIN_LEVEL;

	/**
	 * Character creation
	 */

	@Property(key = "gameserver.character.creation.mode", defaultValue = "0")
	public static int CHARACTER_CREATION_MODE;

	@Property(key = "gameserver.character.limit.count", defaultValue = "8")
	public static int CHARACTER_LIMIT_COUNT;

	@Property(key = "gameserver.character.faction.limitation.mode", defaultValue = "0")
	public static int CHARACTER_FACTION_LIMITATION_MODE;

	@Property(key = "gameserver.ratio.limitation.enable", defaultValue = "false")
	public static boolean ENABLE_RATIO_LIMITATION;

	@Property(key = "gameserver.ratio.min.value", defaultValue = "60")
	public static int RATIO_MIN_VALUE;

	@Property(key = "gameserver.ratio.min.required.level", defaultValue = "10")
	public static int RATIO_MIN_REQUIRED_LEVEL;

	@Property(key = "gameserver.ratio.min.characters_count", defaultValue = "50")
	public static int RATIO_MIN_CHARACTERS_COUNT;

	@Property(key = "gameserver.ratio.high_player_count.disabling", defaultValue = "500")
	public static int RATIO_HIGH_PLAYER_COUNT_DISABLING;

	/**
	 * Misc
	 */

	@Property(key = "gameserver.character.reentry.time", defaultValue = "20")
	public static int CHARACTER_REENTRY_TIME;

	@Property(key = "gameserver.item_wrap_limit", defaultValue = "0")
	public static int ITEM_WRAP_LIMIT;

	@Property(key = "gameserver.web_rewards.enable", defaultValue = "false")
	public static boolean ENABLE_WEB_REWARDS;

	@Property(key = "gameserver.analysis.quest_handlers", defaultValue = "true")
	public static boolean ANALYZE_QUESTHANDLERS;

	/**
	 * Location of quest *.java handlers
	 */
	@Property(key = "gameserver.quest.handler_directory", defaultValue = "./data/handlers/quest")
	public static File QUEST_HANDLER_DIRECTORY;
}
