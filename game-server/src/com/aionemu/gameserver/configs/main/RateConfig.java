package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

/**
 * @author ATracer
 */
public class RateConfig {

	/**
	 * XP Rates - Regular (1), Premium (2), VIP (3)
	 */
	@Property(key = "gameserver.rate.regular.xp", defaultValue = "1.0")
	public static float XP_RATE;

	@Property(key = "gameserver.rate.premium.xp", defaultValue = "2.0")
	public static float PREMIUM_XP_RATE;

	@Property(key = "gameserver.rate.vip.xp", defaultValue = "3.0")
	public static float VIP_XP_RATE;

	/**
	 * Group XP Rates - Regular (1), Premium (2), VIP (3)
	 */
	@Property(key = "gameserver.rate.regular.group.xp", defaultValue = "1.0")
	public static float GROUPXP_RATE;

	@Property(key = "gameserver.rate.premium.group.xp", defaultValue = "2.0")
	public static float PREMIUM_GROUPXP_RATE;

	@Property(key = "gameserver.rate.vip.group.xp", defaultValue = "3.0")
	public static float VIP_GROUPXP_RATE;

	/**
	 * Quest XP Rates - Regular (1), Premium (2), VIP (3)
	 */
	@Property(key = "gameserver.rate.regular.quest.xp", defaultValue = "1.0")
	public static float QUEST_XP_RATE;

	@Property(key = "gameserver.rate.premium.quest.xp", defaultValue = "2.0")
	public static float PREMIUM_QUEST_XP_RATE;

	@Property(key = "gameserver.rate.vip.quest.xp", defaultValue = "3.0")
	public static float VIP_QUEST_XP_RATE;

	/**
	 * Gathering XP Rates - Regular (1), Premium (2), VIP (3)
	 */
	@Property(key = "gameserver.rate.regular.gathering.xp", defaultValue = "1.0")
	public static float GATHERING_XP_RATE;

	@Property(key = "gameserver.rate.premium.gathering.xp", defaultValue = "2.0")
	public static float PREMIUM_GATHERING_XP_RATE;

	@Property(key = "gameserver.rate.vip.gathering.xp", defaultValue = "3.0")
	public static float VIP_GATHERING_XP_RATE;

	/**
	 * Gathering Count Rates - Regular (1), Premium (1), VIP (1)
	 */
	@Property(key = "gameserver.rate.regular.gathering.count", defaultValue = "1")
	public static int GATHERING_COUNT_RATE;

	@Property(key = "gameserver.rate.premium.gathering.count", defaultValue = "1")
	public static int PREMIUM_GATHERING_COUNT_RATE;

	@Property(key = "gameserver.rate.vip.gathering.count", defaultValue = "1")
	public static int VIP_GATHERING_COUNT_RATE;

	/**
	 * Crafting XP Rates - Regular (1), Premium (2), VIP (3)
	 */
	@Property(key = "gameserver.rate.regular.crafting.xp", defaultValue = "1.0")
	public static float CRAFTING_XP_RATE;

	@Property(key = "gameserver.rate.premium.crafting.xp", defaultValue = "2.0")
	public static float PREMIUM_CRAFTING_XP_RATE;

	@Property(key = "gameserver.rate.vip.crafting.xp", defaultValue = "3.0")
	public static float VIP_CRAFTING_XP_RATE;

	/**
	 * Quest Kinah Rates - Regular (1), Premium (2), VIP (3)
	 */
	@Property(key = "gameserver.rate.regular.quest.kinah", defaultValue = "1.0")
	public static float QUEST_KINAH_RATE;

	@Property(key = "gameserver.rate.premium.quest.kinah", defaultValue = "2.0")
	public static float PREMIUM_QUEST_KINAH_RATE;

	@Property(key = "gameserver.rate.vip.quest.kinah", defaultValue = "3.0")
	public static float VIP_QUEST_KINAH_RATE;

	/**
	 * Quest AP Rates - Regular (1), Premium (2), VIP (3)
	 */
	@Property(key = "gameserver.rate.regular.quest.ap", defaultValue = "1.0")
	public static float QUEST_AP_RATE;

	@Property(key = "gameserver.rate.premium.quest.ap", defaultValue = "2.0")
	public static float PREMIUM_QUEST_AP_RATE;

	@Property(key = "gameserver.rate.vip.quest.ap", defaultValue = "3.0")
	public static float VIP_QUEST_AP_RATE;

	/**
	 * Drop Rates - Regular (1), Premium (2), VIP (3)
	 */
	@Property(key = "gameserver.rate.regular.drop", defaultValue = "1.0")
	public static float DROP_RATE;

	@Property(key = "gameserver.rate.premium.drop", defaultValue = "2.0")
	public static float PREMIUM_DROP_RATE;

	@Property(key = "gameserver.rate.vip.drop", defaultValue = "3.0")
	public static float VIP_DROP_RATE;

	/**
	 * Player Abyss Points Rates (Gain) - Regular (1), Premium (2), VIP (3)
	 */
	@Property(key = "gameserver.rate.regular.ap.player.gain", defaultValue = "1.0")
	public static float AP_PLAYER_GAIN_RATE;

	@Property(key = "gameserver.rate.premium.ap.player.gain", defaultValue = "2.0")
	public static float PREMIUM_AP_PLAYER_GAIN_RATE;

	@Property(key = "gameserver.rate.vip.ap.player.gain", defaultValue = "3.0")
	public static float VIP_AP_PLAYER_GAIN_RATE;

	/**
	 * Player Glory Points Rates (Gain) - Regular (1), Premium (2), VIP (3)
	 */
	@Property(key = "gameserver.rate.regular.gp.player.gain", defaultValue = "1.0")
	public static float GP_PLAYER_GAIN_RATE;

	@Property(key = "gameserver.rate.premium.gp.player.gain", defaultValue = "2.0")
	public static float PREMIUM_GP_PLAYER_GAIN_RATE;

	@Property(key = "gameserver.rate.vip.gp.player.gain", defaultValue = "3.0")
	public static float VIP_GP_PLAYER_GAIN_RATE;

	/**
	 * Player Experience Points Rates (Gain) - Regular (1), Premium (2), VIP (3)
	 */
	@Property(key = "gameserver.rate.regular.xp.player.gain", defaultValue = "1.0")
	public static float XP_PLAYER_GAIN_RATE;

	@Property(key = "gameserver.rate.premium.xp.player.gain", defaultValue = "2.0")
	public static float PREMIUM_XP_PLAYER_GAIN_RATE;

	@Property(key = "gameserver.rate.vip.xp.player.gain", defaultValue = "3.0")
	public static float VIP_XP_PLAYER_GAIN_RATE;

	/**
	 * Player Abyss Points Rates - Regular (1), Premium (2), VIP (3)
	 */
	@Property(key = "gameserver.rate.regular.ap.player.loss", defaultValue = "1.0")
	public static float AP_PLAYER_LOSS_RATE;

	@Property(key = "gameserver.rate.premium.ap.player.loss", defaultValue = "2.0")
	public static float PREMIUM_AP_PLAYER_LOSS_RATE;

	@Property(key = "gameserver.rate.vip.ap.player.loss", defaultValue = "3.0")
	public static float VIP_AP_PLAYER_LOSS_RATE;

	/**
	 * NPC Abyss Points Rates - Regular (1), Premium (2), VIP (3)
	 */
	@Property(key = "gameserver.rate.regular.ap.npc", defaultValue = "1.0")
	public static float AP_NPC_RATE;

	@Property(key = "gameserver.rate.premium.ap.npc", defaultValue = "2.0")
	public static float PREMIUM_AP_NPC_RATE;

	@Property(key = "gameserver.rate.vip.ap.npc", defaultValue = "3.0")
	public static float VIP_AP_NPC_RATE;

	/**
	 * PVE DP Rates - Regular (1), Premium (2), VIP (3)
	 */
	@Property(key = "gameserver.rate.regular.dp.npc", defaultValue = "1.0")
	public static float DP_NPC_RATE;

	@Property(key = "gameserver.rate.premium.dp.npc", defaultValue = "2.0")
	public static float PREMIUM_DP_NPC_RATE;

	@Property(key = "gameserver.rate.vip.dp.npc", defaultValue = "3.0")
	public static float VIP_DP_NPC_RATE;

	/**
	 * PVP DP Rates - Regular (1), Premium (2), VIP (3)
	 */
	@Property(key = "gameserver.rate.regular.dp.player", defaultValue = "1.0")
	public static float DP_PLAYER_RATE;

	@Property(key = "gameserver.rate.premium.dp.player", defaultValue = "2.0")
	public static float PREMIUM_DP_PLAYER_RATE;

	@Property(key = "gameserver.rate.vip.dp.player", defaultValue = "3.0")
	public static float VIP_DP_PLAYER_RATE;

	/**
	 * PVP Arena and Dredgion reward rates
	 */
	@Property(key = "gameserver.rate.dredgion", defaultValue = "1.6")
	public static float DREDGION_REWARD_RATE;

	@Property(key = "gameserver.rate.regular.pvparena.discipline", defaultValue = "1")
	public static float PVP_ARENA_DISCIPLINE_REWARD_RATE;

	@Property(key = "gameserver.rate.premium.pvparena.discipline", defaultValue = "1")
	public static float PREMIUM_PVP_ARENA_DISCIPLINE_REWARD_RATE;

	@Property(key = "gameserver.rate.vip.pvparena.discipline", defaultValue = "1")
	public static float VIP_PVP_ARENA_DISCIPLINE_REWARD_RATE;

	@Property(key = "gameserver.rate.regular.pvparena.chaos", defaultValue = "1")
	public static float PVP_ARENA_CHAOS_REWARD_RATE;

	@Property(key = "gameserver.rate.premium.pvparena.chaos", defaultValue = "1")
	public static float PREMIUM_PVP_ARENA_CHAOS_REWARD_RATE;

	@Property(key = "gameserver.rate.vip.pvparena.chaos", defaultValue = "1")
	public static float VIP_PVP_ARENA_CHAOS_REWARD_RATE;

	@Property(key = "gameserver.rate.regular.pvparena.harmony", defaultValue = "1")
	public static float PVP_ARENA_HARMONY_REWARD_RATE;

	@Property(key = "gameserver.rate.premium.pvparena.harmony", defaultValue = "1")
	public static float PREMIUM_PVP_ARENA_HARMONY_REWARD_RATE;

	@Property(key = "gameserver.rate.vip.pvparena.harmony", defaultValue = "1")
	public static float VIP_PVP_ARENA_HARMONY_REWARD_RATE;

	@Property(key = "gameserver.rate.regular.pvparena.glory", defaultValue = "1")
	public static float PVP_ARENA_GLORY_REWARD_RATE;

	@Property(key = "gameserver.rate.premium.pvparena.glory", defaultValue = "1")
	public static float PREMIUM_PVP_ARENA_GLORY_REWARD_RATE;

	@Property(key = "gameserver.rate.vip.pvparena.glory", defaultValue = "1")
	public static float VIP_PVP_ARENA_GLORY_REWARD_RATE;

	@Property(key = "gameserver.rate.regular.idgelresearchcenter.instance", defaultValue = "1")
	public static float IDGEL_RESEARCH_CENTER_INSTANCE_REWARD_RATE;

	@Property(key = "gameserver.rate.premium.idgelresearchcenter.instance", defaultValue = "1")
	public static float PREMIUM_IDGEL_RESEARCH_CENTER_INSTANCE_REWARD_RATE;

	@Property(key = "gameserver.vip.premium.idgelresearchcenter.instance", defaultValue = "1")
	public static float VIP_IDGEL_RESEARCH_CENTER_INSTANCE_REWARD_RATE;

	/**
	 * Sell limits rate
	 */
	@Property(key = "gameserver.rate.regular.sell.limit", defaultValue = "1")
	public static float SELL_LIMIT_RATE;

	@Property(key = "gameserver.rate.premium.sell.limit", defaultValue = "2")
	public static float PREMIUM_SELL_LIMIT_RATE;

	@Property(key = "gameserver.rate.vip.sell.limit", defaultValue = "3")
	public static float VIP_SELL_LIMIT_RATE;

	/**
	 * NPC Glory Points Rates - Regular (1), Premium (2), VIP (3)
	 */
	@Property(key = "gameserver.rate.regular.gp.npc", defaultValue = "1.0")
	public static float GP_NPC_RATE;
	@Property(key = "gameserver.rate.premium.gp.npc", defaultValue = "2.0")
	public static float PREMIUM_GP_NPC_RATE;
	@Property(key = "gameserver.rate.vip.gp.npc", defaultValue = "3.0")
	public static float VIP_GP_NPC_RATE;

}
