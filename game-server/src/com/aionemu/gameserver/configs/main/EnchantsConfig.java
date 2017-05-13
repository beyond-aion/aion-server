package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

public class EnchantsConfig {

	/**
	 * Supplement success rate modifier
	 */
	@Property(key = "gameserver.enchant.supplements.modifier", defaultValue = "1.0")
	public static float SUPPLEMENTS_MODIFIER;

	/**
	 * ManaStone Rates
	 */
	@Property(key = "gameserver.enchant.manastones.chance", defaultValue = "75")
	public static float MANA_STONE_CHANCE;

	@Property(key = "gameserver.enchant.enchantment_stone.base_chance", defaultValue = "65")
	public static float ENCHANTMENT_STONE_BASE_CHANCE;

	@Property(key = "gameserver.enchant.enchantment_stone.amplified_chance", defaultValue = "50")
	public static float ENCHANTMENT_STONE_AMPLIFIED_CHANCE;

	@Property(key = "gameserver.manastone.clean", defaultValue = "false")
	public static boolean CLEAN_STONE;

	@Property(key = "gameserver.tampering.chance", defaultValue = "65")
	public static int TEMPERING_CHANCE;

	@Property(key = "gameserver.max.tampering.level", defaultValue = "0")
	public static int MAX_TAMPERING_LEVEL;
}
