package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

/**
 * @author Neon
 */
public class RatesConfig {

	// Success rates
	@Property(key = "gameserver.rates.crafting.crit_chances", defaultValue = "15.0, 30.0")
	public static float[] CRAFT_CRIT_CHANCES;

	@Property(key = "gameserver.rates.crafting.combo_crit_chances", defaultValue = "25.0, 50.0")
	public static float[] CRAFT_COMBO_CHANCES;

	@Property(key = "gameserver.rates.manastone_chances", defaultValue = "75.0, 75.0")
	public static float[] MANASTONE_CHANCES;

	@Property(key = "gameserver.rates.enchantment_stone.base_chances", defaultValue = "65.0, 65.0")
	public static float[] ENCHANTMENT_STONE_BASE_CHANCES;

	@Property(key = "gameserver.rates.enchantment_stone.amplified_chances", defaultValue = "50.0, 50.0")
	public static float[] ENCHANTMENT_STONE_AMPLIFIED_CHANCES;

	@Property(key = "gameserver.rates.tampering_chances", defaultValue = "65.0, 65.0")
	public static float[] TEMPERING_CHANCES;

	// Modifiers
	@Property(key = "gameserver.rates.xp.solo", defaultValue = "1.0, 2.0")
	public static float[] XP_SOLO_RATES;

	@Property(key = "gameserver.rates.xp.group", defaultValue = "1.0, 2.0")
	public static float[] XP_GROUP_RATES;

	@Property(key = "gameserver.rates.xp.quest", defaultValue = "1.0, 2.0")
	public static float[] XP_QUEST_RATES;

	@Property(key = "gameserver.rates.xp.gathering", defaultValue = "1.0, 2.0")
	public static float[] XP_GATHERING_RATES;

	@Property(key = "gameserver.rates.xp.crafting", defaultValue = "1.0, 2.0")
	public static float[] XP_CRAFTING_RATES;

	@Property(key = "gameserver.rates.xp.pvp", defaultValue = "1.0, 2.0")
	public static float[] XP_PVP_RATES;

	@Property(key = "gameserver.rates.skill_xp.gathering", defaultValue = "1.0, 2.0")
	public static float[] SKILL_XP_GATHERING_RATES;

	@Property(key = "gameserver.rates.skill_xp.crafting", defaultValue = "1.0, 2.0")
	public static float[] SKILL_XP_CRAFTING_RATES;

	@Property(key = "gameserver.rates.ap.pvp.gain", defaultValue = "1.0, 2.0")
	public static float[] AP_PVP_RATES;

	@Property(key = "gameserver.rates.ap.pvp.loss", defaultValue = "1.0, 1.0")
	public static float[] AP_PVP_LOSS_RATES;

	@Property(key = "gameserver.rates.ap.pve", defaultValue = "1.0, 2.0")
	public static float[] AP_PVE_RATES;

	@Property(key = "gameserver.rates.ap.quest", defaultValue = "1.0, 2.0")
	public static float[] AP_QUEST_RATES;

	@Property(key = "gameserver.rates.ap.dredgion", defaultValue = "1.0, 2.0")
	public static float[] AP_DREDGION_RATES;

	@Property(key = "gameserver.rates.gp.gain", defaultValue = "1.0, 2.0")
	public static float[] GP_RATES;

	@Property(key = "gameserver.rates.dp.pve", defaultValue = "1.0, 2.0")
	public static float[] DP_PVE_RATES;

	@Property(key = "gameserver.rates.dp.pvp", defaultValue = "1.0, 2.0")
	public static float[] DP_PVP_RATES;

	@Property(key = "gameserver.rates.kinah.quest", defaultValue = "1.0, 2.0")
	public static float[] QUEST_KINAH_RATES;

	@Property(key = "gameserver.rates.drop", defaultValue = "1.0, 2.0")
	public static float[] DROP_RATES;

	@Property(key = "gameserver.rates.gathering.count", defaultValue = "1.0, 2.0")
	public static float[] GATHERING_COUNT_RATES;

	@Property(key = "gameserver.rates.pvparena.discipline", defaultValue = "1.0, 2.0")
	public static float[] PVP_ARENA_DISCIPLINE_REWARD_RATES;

	@Property(key = "gameserver.rates.pvparena.chaos", defaultValue = "1.0, 2.0")
	public static float[] PVP_ARENA_CHAOS_REWARD_RATES;

	@Property(key = "gameserver.rates.pvparena.harmony", defaultValue = "1.0, 2.0")
	public static float[] PVP_ARENA_HARMONY_REWARD_RATES;

	@Property(key = "gameserver.rates.pvparena.glory", defaultValue = "1.0, 2.0")
	public static float[] PVP_ARENA_GLORY_REWARD_RATES;

	@Property(key = "gameserver.rates.sell_limit", defaultValue = "1.0, 2.0")
	public static float[] SELL_LIMIT_RATES;

}
