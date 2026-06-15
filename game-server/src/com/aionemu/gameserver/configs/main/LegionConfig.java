package com.aionemu.gameserver.configs.main;

import java.util.regex.Pattern;

import com.aionemu.commons.configuration.Property;

/**
 * @author Simple
 */
public class LegionConfig {

	/**
	 * Announcement pattern (checked when announcement is being created)
	 */
	@Property(key = "gameserver.legion.pattern", defaultValue = "[a-zA-Z ]{2,32}")
	public static Pattern LEGION_NAME_PATTERN;

	/**
	 * Self Intro pattern (checked when self intro is being changed)
	 */
	@Property(key = "gameserver.legion.selfintropattern", defaultValue = ".{1,32}")
	public static Pattern SELF_INTRO_PATTERN;

	/**
	 * Nickname pattern (checked when nickname is being changed)
	 */
	@Property(key = "gameserver.legion.nicknamepattern", defaultValue = ".{1,10}")
	public static Pattern NICKNAME_PATTERN;

	/**
	 * Sets disband legion time
	 */
	@Property(key = "gameserver.legion.disbandtime", defaultValue = "86400")
	public static int LEGION_DISBAND_TIME;

	/**
	 * Sets required kinah to create a legion
	 */
	@Property(key = "gameserver.legion.creationrequiredkinah", defaultValue = "10000")
	public static int LEGION_CREATE_REQUIRED_KINAH;

	/**
	 * Sets required kinah to create emblem
	 */
	@Property(key = "gameserver.legion.emblemrequiredkinah", defaultValue = "800000")
	public static int LEGION_EMBLEM_REQUIRED_KINAH;

	/**
	 * Sets required kinah to level legion up to 2
	 */
	@Property(key = "gameserver.legion.level2requiredkinah", defaultValue = "100000")
	public static int LEGION_LEVEL2_REQUIRED_KINAH;

	/**
	 * Sets required kinah to level legion up to 3
	 */
	@Property(key = "gameserver.legion.level3requiredkinah", defaultValue = "1000000")
	public static int LEGION_LEVEL3_REQUIRED_KINAH;

	/**
	 * Sets required kinah to level legion up to 4
	 */
	@Property(key = "gameserver.legion.level4requiredkinah", defaultValue = "5000000")
	public static int LEGION_LEVEL4_REQUIRED_KINAH;

	/**
	 * Sets required kinah to level legion up to 5
	 */
	@Property(key = "gameserver.legion.level5requiredkinah", defaultValue = "25000000")
	public static int LEGION_LEVEL5_REQUIRED_KINAH;

	/**
	 * Sets required kinah to level legion up to 6
	 */
	@Property(key = "gameserver.legion.level6requiredkinah", defaultValue = "50000000")
	public static int LEGION_LEVEL6_REQUIRED_KINAH;

	/**
	 * Sets required kinah to level legion up to 7
	 */
	@Property(key = "gameserver.legion.level7requiredkinah", defaultValue = "75000000")
	public static int LEGION_LEVEL7_REQUIRED_KINAH;

	/**
	 * Sets required kinah to level legion up to 8
	 */
	@Property(key = "gameserver.legion.level8requiredkinah", defaultValue = "100000000")
	public static int LEGION_LEVEL8_REQUIRED_KINAH;

	/**
	 * Sets required amount of members to level legion up to 2
	 */
	@Property(key = "gameserver.legion.level2requiredmembers", defaultValue = "10")
	public static int LEGION_LEVEL2_REQUIRED_MEMBERS;

	/**
	 * Sets required amount of members to level legion up to 3
	 */
	@Property(key = "gameserver.legion.level3requiredmembers", defaultValue = "20")
	public static int LEGION_LEVEL3_REQUIRED_MEMBERS;

	/**
	 * Sets required amount of members to level legion up to 4
	 */
	@Property(key = "gameserver.legion.level4requiredmembers", defaultValue = "30")
	public static int LEGION_LEVEL4_REQUIRED_MEMBERS;

	/**
	 * Sets required amount of members to level legion up to 5
	 */
	@Property(key = "gameserver.legion.level5requiredmembers", defaultValue = "40")
	public static int LEGION_LEVEL5_REQUIRED_MEMBERS;

	/**
	 * Sets required amount of members to level legion up to 6
	 */
	@Property(key = "gameserver.legion.level6requiredmembers", defaultValue = "50")
	public static int LEGION_LEVEL6_REQUIRED_MEMBERS;

	/**
	 * Sets required amount of members to level legion up to 7
	 */
	@Property(key = "gameserver.legion.level7requiredmembers", defaultValue = "60")
	public static int LEGION_LEVEL7_REQUIRED_MEMBERS;

	/**
	 * Sets required amount of members to level legion up to 8
	 */
	@Property(key = "gameserver.legion.level8requiredmembers", defaultValue = "70")
	public static int LEGION_LEVEL8_REQUIRED_MEMBERS;

	/**
	 * Sets required amount of abyss point to level legion up to 2
	 */
	@Property(key = "gameserver.legion.level2requiredcontribution", defaultValue = "0")
	public static int LEGION_LEVEL2_REQUIRED_CONTRIBUTION;

	/**
	 * Sets required amount of abyss point to level legion up to 3
	 */
	@Property(key = "gameserver.legion.level3requiredcontribution", defaultValue = "20000")
	public static int LEGION_LEVEL3_REQUIRED_CONTRIBUTION;

	/**
	 * Sets required amount of abyss point to level legion up to 4
	 */
	@Property(key = "gameserver.legion.level4requiredcontribution", defaultValue = "100000")
	public static int LEGION_LEVEL4_REQUIRED_CONTRIBUTION;

	/**
	 * Sets required amount of abyss point to level legion up to 5
	 */
	@Property(key = "gameserver.legion.level5requiredcontribution", defaultValue = "500000")
	public static int LEGION_LEVEL5_REQUIRED_CONTRIBUTION;

	/**
	 * Sets required amount of abyss point to level legion up to 6
	 */
	@Property(key = "gameserver.legion.level6requiredcontribution", defaultValue = "2500000")
	public static int LEGION_LEVEL6_REQUIRED_CONTRIBUTION;

	/**
	 * Sets required amount of abyss point to level legion up to 7
	 */
	@Property(key = "gameserver.legion.level7requiredcontribution", defaultValue = "12500000")
	public static int LEGION_LEVEL7_REQUIRED_CONTRIBUTION;

	/**
	 * Sets required amount of abyss point to level legion up to 8
	 */
	@Property(key = "gameserver.legion.level8requiredcontribution", defaultValue = "62500000")
	public static int LEGION_LEVEL8_REQUIRED_CONTRIBUTION;

	/**
	 * Sets max members of a level 1 legion
	 */
	@Property(key = "gameserver.legion.level1maxmembers", defaultValue = "30")
	public static int LEGION_LEVEL1_MAX_MEMBERS;

	/**
	 * Sets max members of a level 2 legion
	 */
	@Property(key = "gameserver.legion.level2maxmembers", defaultValue = "60")
	public static int LEGION_LEVEL2_MAX_MEMBERS;

	/**
	 * Sets max members of a level 3 legion
	 */
	@Property(key = "gameserver.legion.level3maxmembers", defaultValue = "90")
	public static int LEGION_LEVEL3_MAX_MEMBERS;

	/**
	 * Sets max members of a level 4 legion
	 */
	@Property(key = "gameserver.legion.level4maxmembers", defaultValue = "120")
	public static int LEGION_LEVEL4_MAX_MEMBERS;

	/**
	 * Sets max members of a level 5 legion
	 */
	@Property(key = "gameserver.legion.level5maxmembers", defaultValue = "150")
	public static int LEGION_LEVEL5_MAX_MEMBERS;

	/**
	 * Sets max members of a level 6 legion
	 */
	@Property(key = "gameserver.legion.level6maxmembers", defaultValue = "180")
	public static int LEGION_LEVEL6_MAX_MEMBERS;

	/**
	 * Sets max members of a level 7 legion
	 */
	@Property(key = "gameserver.legion.level7maxmembers", defaultValue = "210")
	public static int LEGION_LEVEL7_MAX_MEMBERS;

	/**
	 * Sets max members of a level 8 legion
	 */
	@Property(key = "gameserver.legion.level8maxmembers", defaultValue = "240")
	public static int LEGION_LEVEL8_MAX_MEMBERS;

	/**
	 * Enable/disable Legion Warehouse
	 */
	@Property(key = "gameserver.legion.warehouse", defaultValue = "true")
	public static boolean LEGION_WAREHOUSE;

	/**
	 * Enable/disable Legion Invite Other Faction
	 */
	@Property(key = "gameserver.legion.inviteotherfaction", defaultValue = "false")
	public static boolean LEGION_INVITEOTHERFACTION;

	@Property(key = "gameserver.legion.task.requirement.enable", defaultValue = "true")
	public static boolean ENABLE_GUILD_TASK_REQ;

	/**
	 * Enable/Disable legion dominion key requirement
	 */
	@Property(key = "gameserver.legion.require_key_for_stonespear_reach", defaultValue = "true")
	public static boolean REQUIRE_KEY_FOR_STONESPEAR_REACH;

	/**
	 * Min points to be reached in stonespear reach instance to account for a territory election
	 */
	@Property(key = "gameserver.legion.stonespear_reach_min_points", defaultValue = "0")
	public static int STONESPEAR_REACH_MIN_POINTS_FOR_TERRITORY;

}
