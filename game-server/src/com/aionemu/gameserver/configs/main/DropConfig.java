package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

/**
 * @author Tiger0319
 */
public class DropConfig {

	/**
	 * Disable drop rate reduction based on level diference between players and mobs
	 */
	@Property(key = "gameserver.drop.reduction.disable", defaultValue = "false")
	public static boolean DISABLE_DROP_REDUCTION;

	/**
	 * Enable announce when a player drops Unique / Epic item
	 */
	@Property(key = "gameserver.unique.drop.announce.enable", defaultValue = "true")
	public static boolean ENABLE_UNIQUE_DROP_ANNOUNCE;
	
	/**
	 * Disable drop rate reduction based on level difference in zone
	 */
	@Property(key = "gameserver.drop.noreduction", defaultValue = "0")
	public static String DISABLE_DROP_REDUCTION_IN_ZONES;

	/**
	 * Enable Global Drops Engine
	 */
	@Property(key = "gameserver.drop.enable.globaldrops", defaultValue = "false")
	public static boolean ENABLE_GLOBAL_DROPS;

	/**
	 * Enable/Disable support for new drop category calculator
	 * if enabled all drops from every single category will be checked 
	 * if disabled only 1 random drop will be selected and then % will be checked on it 
	*/
	@Property(key = "gameserver.enable.support.new.drop.category.calculator", defaultValue = "false")
	public static boolean DROP_ENABLE_SUPPORT_NEW_DROP_CATEGORY_CALCULATION;

	/**
	 * Enable/Disable support for new npc_drops.dat files
	 * if disabled old npc_drops.dat files will be used
	 */
	@Property(key = "gameserver.enable.support.new.npcdrops.files", defaultValue = "true")
	public static boolean DROP_ENABLE_SUPPORT_NEW_NPCDROPS_FILES;
}
