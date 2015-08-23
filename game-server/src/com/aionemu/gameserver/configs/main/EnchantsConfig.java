package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

public class EnchantsConfig {

	/**
	 * Supplement Additional Rates
	 */
	@Property(key = "gameserver.supplement.lesser", defaultValue = "1.0")
	public static float LESSER_SUP;
	@Property(key = "gameserver.supplement.regular", defaultValue = "1.0")
	public static float REGULAR_SUP;
	@Property(key = "gameserver.supplement.greater", defaultValue = "1.0")
	public static float GREATER_SUP;
	@Property(key = "gameserver.supplement.mythic", defaultValue = "1.0")
	public static float MYTHIC_SUP;

	/**
	 * ManaStone Rates
	 */
	@Property(key = "gameserver.base.manastone", defaultValue = "50")
	public static float MANA_STONE;
	
	@Property(key = "gameserver.enchant.amplified.chance", defaultValue = "40")
	public static short AMPLIFIED_ENCHANT_CHANCE;
	
	@Property(key = "gameserver.manastone.clean", defaultValue = "false")
	public static boolean CLEAN_STONE;
	
	@Property(key = "gameserver.tampering.chance", defaultValue = "65")
	public static int TAMPERING_CHANCE;
	
	@Property(key = "use.hard.tampering.formula.for.plume", defaultValue = "true")
	public static boolean USE_HARD_TAMPERING_FORMULA_FOR_PLUME;
	
	@Property(key = "gameserver.max.tampering.level", defaultValue = "0")
	public static int MAX_TAMPERING_LEVEL;

	@Property(key = "gameserver.max.amplification.level", defaultValue = "20")
	public static int MAX_AMPLIFICATION_LEVEL;
}
