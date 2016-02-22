package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

public class FallDamageConfig {

	/**
	 * Percentage of damage per meter.
	 */
	@Property(key = "gameserver.falldamage.percentage", defaultValue = "1.0")
	public static float FALL_DAMAGE_PERCENTAGE;

	/**
	 * Minimum fall damage range
	 */
	@Property(key = "gameserver.falldamage.distance.minimum", defaultValue = "10")
	public static int MINIMUM_DISTANCE_DAMAGE;

	/**
	 * Maximum fall distance after which you will die after hitting the ground.
	 */
	@Property(key = "gameserver.falldamage.distance.maximum", defaultValue = "50")
	public static int MAXIMUM_DISTANCE_DAMAGE;

	/**
	 * Maximum fall distance after which you will die in mid air.
	 */
	@Property(key = "gameserver.falldamage.distance.midair", defaultValue = "200")
	public static int MAXIMUM_DISTANCE_MIDAIR;
}
