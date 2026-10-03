package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

public class GeoDataConfig {

	/**
	 * Geo data mode
	 */
	@Property(key = "gameserver.geodata.mode", defaultValue = "ON")
	public static Mode MODE;

	/**
	 * Enable canSee checks using geodata.
	 */
	@Property(key = "gameserver.geodata.cansee.enable", defaultValue = "true")
	public static boolean CANSEE_ENABLE;

	/**
	 * Enable Fear skill using geodata.
	 */
	@Property(key = "gameserver.geodata.fear.enable", defaultValue = "true")
	public static boolean FEAR_ENABLE;

	/**
	 * Enable Geo checks during npc movement (prevent flying mobs)
	 */
	@Property(key = "gameserver.geodata.npc.move", defaultValue = "true")
	public static boolean GEO_NPC_MOVE;

	/**
	 * Show collision zone name and skill id
	 */
	@Property(key = "gameserver.geodata.materials.showdetails", defaultValue = "false")
	public static boolean GEO_MATERIALS_SHOWDETAILS;

	public enum Mode {
		/**
		 * Skips loading of geo data entirely, so obstacle detection, terrain checks and environment effects, such as lava damage, fortress shields or EoR recovery zones will not work.<br>
		 * Use for testing or in memory constrained environments.
		 */
		OFF,
		/**
		 * Skips loading of most geo data, so obstacle detection, terrain checks and terrain-based effects, such as lava damage, will not work. Geometries that trigger skills are loaded (shields, EoR regenerating zones, etc.).<br>
		 * Use for testing or in memory constrained environments.
		 */
		MATERIALS_ONLY,
		/**
		 * All geo data is loaded and validated.
		 */
		ON
	}
}
