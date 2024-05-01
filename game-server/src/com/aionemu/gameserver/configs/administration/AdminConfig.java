package com.aionemu.gameserver.configs.administration;

import java.util.List;

import com.aionemu.commons.configuration.Property;

/**
 * @author ATracer, Neon
 */
public class AdminConfig {

	/**
	 * Custom name tags based on access level. The first entry is the tag for access level 1, then 2, 3 and so on.<br>
	 * Default:
	 * 
	 * <pre>
	 * Access level 1 = [empty]
	 * Access level 2 = Junior Dev
	 * Access level 3 = Dev
	 * Access level 4 = Junior Event Master
	 * Access level 5 = Event Master
	 * Access level 6 = Junior Game Master
	 * Access level 7 = Game Master
	 * Access level 8 = Senior Game Master
	 * Access level 9 = Admin
	 * </pre>
	 */
	@Property(key = "gameserver.administration.customtags",
		defaultValue = "%s, \u00BBJDev\u00AB\uE04A%s, \u00BBDev\u00AB\uE04A%s, \u00BBJEM\u00AB\uE04A%s, \u00BBEM\u00AB\uE04A%s, \u00BBJGM\u00AB\uE04A%s, \u00BBGM\u00AB\uE04A%s, \u00BBSGM\u00AB\uE04A%s, \u00BBAdmin\u00AB\uE04A%s")
	public static String[] NAME_TAGS;

	/**
	 * Admin properties
	 */
	@Property(key = "gameserver.administration.unrestricted_itemtrade", defaultValue = "1")
	public static byte UNRESTRICTED_ITEMTRADE;
	@Property(key = "gameserver.administration.gm_panel", defaultValue = "2")
	public static byte GM_PANEL;
	@Property(key = "gameserver.administration.gm_skills", defaultValue = "8")
	public static byte GM_SKILLS;
	@Property(key = "gameserver.administration.flight.free_fly", defaultValue = "1")
	public static byte FREE_FLIGHT;
	@Property(key = "gameserver.administration.flight.unlimited_time", defaultValue = "1")
	public static byte UNLIMITED_FLIGHT_TIME;
	@Property(key = "gameserver.administration.auto_res", defaultValue = "1")
	public static byte AUTO_RES;
	@Property(key = "gameserver.administration.view_player_details", defaultValue = "5")
	public static byte VIEW_PLAYER_DETAILS;
	@Property(key = "gameserver.administration.instance.enter_all", defaultValue = "2")
	public static byte INSTANCE_ENTER_ALL;
	@Property(key = "gameserver.administration.instance.open_doors", defaultValue = "6")
	public static byte INSTANCE_OPEN_DOORS;
	@Property(key = "gameserver.administration.instance.door_info", defaultValue = "9")
	public static byte INSTANCE_DOOR_INFO;
	@Property(key = "gameserver.administration.house.enter_all", defaultValue = "9")
	public static byte HOUSE_ENTER_ALL;
	@Property(key = "gameserver.administration.house.show_address", defaultValue = "9")
	public static byte HOUSE_SHOW_ADDRESS;
	@Property(key = "gameserver.administration.dialog_info", defaultValue = "9")
	public static byte DIALOG_INFO;
	@Property(key = "gameserver.administration.enchant_info", defaultValue = "9")
	public static byte ENCHANT_INFO;
	@Property(key = "gameserver.administration.zone_info", defaultValue = "9")
	public static byte ZONE_INFO;
	@Property(key = "gameserver.administration.audit_info", defaultValue = "9")
	public static byte AUDIT_INFO;

	/**
	 * Special command permissions
	 */
	@Property(key = "gameserver.administration.command.quest.advanced_parameters", defaultValue = "9")
	public static byte CMD_QUEST_ADV_PARAMS;

	/**
	 * Login/logout options
	 */
	@Property(key = "gameserver.administration.login.execute_commands", defaultValue = "//invis, //invul, //enemy none, //see")
	public static List<String> LOGIN_EXECUTE_COMMANDS;
	@Property(key = "gameserver.administration.login.print_revision", defaultValue = "9")
	public static byte REVISION_INFO_ON_LOGIN;
	@Property(key = "gameserver.administration.login.announce_levels", defaultValue = "*")
	public static List<String> ANNOUNCE_LEVELS;
	@Property(key = "gameserver.administration.login.announce_to_all_players", defaultValue = "true")
	public static boolean ANNOUNCE_LOGIN_TO_ALL_PLAYERS;
	@Property(key = "gameserver.administration.logout.announce_to_all_players", defaultValue = "true")
	public static boolean ANNOUNCE_LOGOUT_TO_ALL_PLAYERS;
}
