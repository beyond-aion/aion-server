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

	/**
	 * Allow applying for or registering instance groups in the Find Group window even if you're not at the instance entrance (like in version 6.2+)
	 */
	@Property(key = "gameserver.instance_group.form_anywhere", defaultValue = "false")
	public static boolean FORM_INSTANCE_GROUP_ANYWHERE;

}
