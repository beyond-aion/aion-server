package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

public class CraftConfig {

	/**
	 * Enable craft skills unrestricted level-up
	 */
	@Property(key = "gameserver.craft.skills.unrestricted.levelup.enable", defaultValue = "false")
	public static boolean UNABLE_CRAFT_SKILLS_UNRESTRICTED_LEVELUP;

	@Property(key = "gameserver.craft.skills.delete.excess.enable", defaultValue = "false")
	public static boolean DELETE_EXCESS_CRAFT_ENABLE;

	/**
	 * Maximum number of expert skills a player can have
	 */
	@Property(key = "gameserver.craft.max.expert.skills", defaultValue = "2")
	public static int MAX_EXPERT_CRAFTING_SKILLS;

	/**
	 * Maximum number of master skills a player can have
	 */
	@Property(key = "gameserver.craft.max.master.skills", defaultValue = "1")
	public static int MAX_MASTER_CRAFTING_SKILLS;

	/**
	 * Chance to have a critical procraft (applied on first step)
	 */
	@Property(key = "gameserver.craft.critical.rate.regular", defaultValue = "15")
	public static int CRAFT_CRIT_RATE;
	@Property(key = "gameserver.craft.critical.rate.premium", defaultValue = "15")
	public static int PREMIUM_CRAFT_CRIT_RATE;
	@Property(key = "gameserver.craft.critical.rate.vip", defaultValue = "15")
	public static int VIP_CRAFT_CRIT_RATE;

	/**
	 * Chance to have a combo procraft (applied on second step)
	 */
	@Property(key = "gameserver.craft.combo.rate.regular", defaultValue = "25")
	public static int CRAFT_COMBO_RATE;
	@Property(key = "gameserver.craft.combo.rate.premium", defaultValue = "25")
	public static int PREMIUM_CRAFT_COMBO_RATE;
	@Property(key = "gameserver.craft.combo.rate.vip", defaultValue = "25")
	public static int VIP_CRAFT_COMBO_RATE;
	@Property(key = "gameserver.craft.fail.chance", defaultValue = "35")
	public static int MAX_CRAFT_FAILURE_CHANCE;
	@Property(key = "gameserver.gather.fail.chance", defaultValue = "38")
	public static int MAX_GATHER_FAILURE_CHANCE;
}
