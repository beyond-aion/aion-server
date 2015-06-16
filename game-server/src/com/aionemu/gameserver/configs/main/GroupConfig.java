package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

public class GroupConfig {

	/**
	 * Group remove time
	 */
	@Property(key = "gameserver.playergroup.removetime", defaultValue = "600")
	public static int GROUP_REMOVE_TIME;

	/**
	 * Group max distance
	 */
	@Property(key = "gameserver.playergroup.maxdistance", defaultValue = "100")
	public static int GROUP_MAX_DISTANCE;

	/**
	 * Enable Group Invite Other Faction
	 */
	@Property(key = "gameserver.group.inviteotherfaction", defaultValue = "false")
	public static boolean GROUP_INVITEOTHERFACTION;

	/**
	 * Alliance remove time
	 */
	@Property(key = "gameserver.playeralliance.removetime", defaultValue = "600")
	public static int ALLIANCE_REMOVE_TIME;

	/**
	 * Enable Alliance Invite Other Faction
	 */
	@Property(key = "gameserver.playeralliance.inviteotherfaction", defaultValue = "false")
	public static boolean ALLIANCE_INVITEOTHERFACTION;
	
	@Property(key = "gameserver.team2.enable", defaultValue = "false")
	public static boolean TEAM2_ENABLE;
}
