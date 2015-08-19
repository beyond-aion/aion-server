package com.aionemu.gameserver.configs.administration;

import com.aionemu.commons.configuration.Property;

/**
 * @author ATracer
 */
public class AdminConfig {

	/**
	 * Admin properties
	 */
	@Property(key = "gameserver.administration.gmlevel", defaultValue = "3")
	public static int GM_LEVEL;
	@Property(key = "gameserver.administration.gmpanel", defaultValue = "3")
	public static int GM_PANEL;
	@Property(key = "gameserver.administration.flight.freefly", defaultValue = "3")
	public static int GM_FLIGHT_FREE;
	@Property(key = "gameserver.administration.flight.unlimited", defaultValue = "3")
	public static int GM_FLIGHT_UNLIMITED;
	@Property(key = "gameserver.administration.doors.opening", defaultValue = "3")
	public static int DOORS_OPEN;
	@Property(key = "gameserver.administration.auto.res", defaultValue = "3")
	public static int ADMIN_AUTO_RES;
	@Property(key = "gameserver.administration.instancereq", defaultValue = "3")
	public static int INSTANCE_REQ;
	@Property(key = "gameserver.administration.view.player", defaultValue = "3")
	public static int ADMIN_VIEW_DETAILS;

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
	public static String ANNOUNCE_LEVEL_LIST;
	@Property(key = "gameserver.administration.announce.login_to_all_players", defaultValue = "true")
	public static boolean ANNOUNCE_LOGIN_TO_ALL_PLAYERS;
	@Property(key = "gameserver.administration.announce.logout_to_all_players", defaultValue = "true")
	public static boolean ANNOUNCE_LOGOUT_TO_ALL_PLAYERS;
	
	@Property(key = "gameserver.administration.trade.item.restriction", defaultValue = "false")
	public static boolean ENABLE_TRADEITEM_RESTRICTION;

	/**
	 * Custom TAG based on access level
	 */
	@Property(key = "gameserver.customtag.enable", defaultValue = "false")
	public static boolean CUSTOMTAG_ENABLE;
	@Property(key = "gameserver.customtag.access1", defaultValue = "<GM> %s")
	public static String CUSTOMTAG_ACCESS1;
	@Property(key = "gameserver.customtag.access2", defaultValue = "<HEADGM> %s")
	public static String CUSTOMTAG_ACCESS2;
	@Property(key = "gameserver.customtag.access3", defaultValue = "<ADMIN> %s")
	public static String CUSTOMTAG_ACCESS3;
	@Property(key = "gameserver.customtag.access4", defaultValue = "<TAG_HERE> %s")
	public static String CUSTOMTAG_ACCESS4;
	@Property(key = "gameserver.customtag.access5", defaultValue = "<TAG_HERE> %s")
	public static String CUSTOMTAG_ACCESS5;
	@Property(key = "gameserver.customtag.access6", defaultValue = "<TAG_HERE> %s")
	public static String CUSTOMTAG_ACCESS6;
	@Property(key = "gameserver.customtag.access7", defaultValue = "<TAG_HERE> %s")
	public static String CUSTOMTAG_ACCESS7;
	@Property(key = "gameserver.customtag.access8", defaultValue = "<TAG_HERE> %s")
	public static String CUSTOMTAG_ACCESS8;
	@Property(key = "gameserver.customtag.access9", defaultValue = "<TAG_HERE> %s")
	public static String CUSTOMTAG_ACCESS9;
}
