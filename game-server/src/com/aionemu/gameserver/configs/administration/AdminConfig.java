package com.aionemu.gameserver.configs.administration;

import java.util.List;

import com.aionemu.commons.configuration.Property;

/**
 * @author ATracer
 */
public class AdminConfig {

	/**
	 * Admin properties
	 */
	@Property(key = "gameserver.administration.gmlevel", defaultValue = "3")
	public static byte GM_LEVEL;
	@Property(key = "gameserver.administration.gmpanel", defaultValue = "3")
	public static byte GM_PANEL;
	@Property(key = "gameserver.administration.gmskills", defaultValue = "3")
	public static byte GM_SKILLS;
	@Property(key = "gameserver.administration.flight.freefly", defaultValue = "3")
	public static byte GM_FLIGHT_FREE;
	@Property(key = "gameserver.administration.flight.unlimited", defaultValue = "3")
	public static byte GM_FLIGHT_UNLIMITED;
	@Property(key = "gameserver.administration.doors.opening", defaultValue = "3")
	public static byte DOORS_OPEN;
	@Property(key = "gameserver.administration.houses.enter_all", defaultValue = "3")
	public static byte HOUSES_ENTER;
	@Property(key = "gameserver.administration.houses.show_address", defaultValue = "3")
	public static byte HOUSES_SHOW_ADDRESS;
	@Property(key = "gameserver.administration.auto.res", defaultValue = "3")
	public static byte ADMIN_AUTO_RES;
	@Property(key = "gameserver.administration.instancereq", defaultValue = "3")
	public static byte INSTANCE_REQ;
	@Property(key = "gameserver.administration.view.player", defaultValue = "3")
	public static byte VIEW_PLAYER_DETAILS;
	@Property(key = "gameserver.administration.dialog_info", defaultValue = "3")
	public static byte DIALOG_INFO;
	@Property(key = "gameserver.administration.enchant_info", defaultValue = "3")
	public static byte ENCHANT_INFO;

	/**
	 * Admin options
	 */
	@Property(key = "gameserver.administration.invis.gm.connection", defaultValue = "false")
	public static boolean INVISIBLE_GM_CONNECTION;
	@Property(key = "gameserver.administration.enemity.gm.connection", defaultValue = "Normal")
	public static String ENEMITY_MODE_GM_CONNECTION;
	@Property(key = "gameserver.administration.invul.gm.connection", defaultValue = "false")
	public static boolean INVULNERABLE_GM_CONNECTION;
	@Property(key = "gameserver.administration.vision.gm.connection", defaultValue = "false")
	public static boolean VISION_GM_CONNECTION;
	@Property(key = "gameserver.administration.whisper.gm.connection", defaultValue = "false")
	public static boolean WHISPER_GM_CONNECTION;

	/**
	 * GM announce options
	 */
	@Property(key = "gameserver.administration.announce.levels", defaultValue = "*")
	public static List<String> ANNOUNCE_LEVEL_LIST;
	@Property(key = "gameserver.administration.announce.login_to_all_players", defaultValue = "true")
	public static boolean ANNOUNCE_LOGIN_TO_ALL_PLAYERS;
	@Property(key = "gameserver.administration.announce.logout_to_all_players", defaultValue = "true")
	public static boolean ANNOUNCE_LOGOUT_TO_ALL_PLAYERS;

	@Property(key = "gameserver.administration.trade.item.restriction", defaultValue = "false")
	public static boolean ENABLE_TRADEITEM_RESTRICTION;

	/**
	 * Custom name tags based on access level
	 */
	@Property(key = "gameserver.administration.customtags",
		defaultValue = "%s, \u00BBJDev\u00AB\uE04A%s, \u00BBDev\u00AB\uE04A%s, \u00BBJEM\u00AB\uE04A%s, \u00BBEM\u00AB\uE04A%s, \u00BBJGM\u00AB\uE04A%s, \u00BBGM\u00AB\uE04A%s, \u00BBSGM\u00AB\uE04A%s, \u00BBAdmin\u00AB\uE04A%s")
	public static String[] NAME_TAGS;

	/**
	 * Special command permissions
	 */
	@Property(key = "gameserver.administration.command.quest.advanced_parameters", defaultValue = "3")
	public static byte CMD_QUEST_ADV_PARAMS;
}
