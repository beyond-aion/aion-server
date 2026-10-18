package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

public class FallDamageConfig {

	/**
	 * Damage per meter of fall height in percent of max HP.
	 */
	@Property(key = "gameserver.falldamage.percentage", defaultValue = "1.0")
	public static float FALL_DAMAGE_PERCENTAGE;

	/**
	 * Flat damage per meter of fall height, added to the percentage.
	 */
	@Property(key = "gameserver.falldamage.flat", defaultValue = "4")
	public static float FALL_DAMAGE_FLAT;

	/**
	 * Fall height above which you take damage when you hit the ground.
	 */
	@Property(key = "gameserver.falldamage.distance.minimum", defaultValue = "10")
	public static float MINIMUM_DISTANCE_DAMAGE;

	/**
	 * Fall height from which you die when you hit the ground.
	 */
	@Property(key = "gameserver.falldamage.distance.maximum", defaultValue = "50")
	public static float MAXIMUM_DISTANCE_DAMAGE;

	/**
	 * Fall height above which you die in mid air if there is no ground to stand on within twice that height below you.
	 */
	@Property(key = "gameserver.falldamage.distance.midair", defaultValue = "50")
	public static float MAXIMUM_DISTANCE_MIDAIR;

	/**
	 * Same as {@link #MAXIMUM_DISTANCE_MIDAIR}, but in fly zones.
	 */
	@Property(key = "gameserver.falldamage.distance.midair.fly_zone", defaultValue = "100")
	public static float MAXIMUM_DISTANCE_MIDAIR_FLY_ZONE;
}
