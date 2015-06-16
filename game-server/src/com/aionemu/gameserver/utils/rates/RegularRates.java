package com.aionemu.gameserver.utils.rates;

import com.aionemu.gameserver.configs.main.CraftConfig;
import com.aionemu.gameserver.configs.main.RateConfig;

/**
 * @author ATracer
 */
public class RegularRates extends Rates {

	@Override
	public float getGroupXpRate() {
		return RateConfig.GROUPXP_RATE;
	}

	@Override
	public float getDropRate() {
		return RateConfig.DROP_RATE;
	}

	@Override
	public float getApNpcRate() {
		return RateConfig.AP_NPC_RATE;
	}

	@Override
	public float getApPlayerGainRate() {
		return RateConfig.AP_PLAYER_GAIN_RATE;
	}
	
	@Override
	public float getGpPlayerGainRate() {
		return RateConfig.GP_PLAYER_GAIN_RATE;
	}

	@Override
	public float getXpPlayerGainRate() {
		return RateConfig.XP_PLAYER_GAIN_RATE;
	}

	@Override
	public float getApPlayerLossRate() {
		return RateConfig.AP_PLAYER_LOSS_RATE;
	}

	@Override
	public float getQuestKinahRate() {
		return RateConfig.QUEST_KINAH_RATE;
	}

	@Override
	public float getQuestXpRate() {
		return RateConfig.QUEST_XP_RATE;
	}

	@Override
	public float getQuestApRate() {
		return RateConfig.QUEST_AP_RATE;
	}

	@Override
	public float getXpRate() {
		return RateConfig.XP_RATE;
	}

	/*
	 * (non-Javadoc) @see
	 * com.aionemu.gameserver.utils.rates.Rates#getCraftingXPRate()
	 */
	@Override
	public float getCraftingXPRate() {
		return RateConfig.CRAFTING_XP_RATE;
	}

	/*
	 * (non-Javadoc) @see
	 * com.aionemu.gameserver.utils.rates.Rates#getGatheringXPRate()
	 */
	@Override
	public float getGatheringXPRate() {
		return RateConfig.GATHERING_XP_RATE;
	}

	@Override
	public int getGatheringCountRate() {
		return RateConfig.GATHERING_COUNT_RATE;
	}

	@Override
	public float getDpNpcRate() {
		return RateConfig.DP_NPC_RATE;
	}

	@Override
	public float getDpPlayerRate() {
		return RateConfig.DP_PLAYER_RATE;
	}

	@Override
	public int getCraftCritRate() {
		return CraftConfig.CRAFT_CRIT_RATE;
	}

	@Override
	public int getComboCritRate() {
		return CraftConfig.CRAFT_COMBO_RATE;
	}
	
	@Override
	public float getDisciplineRewardRate() {
		return RateConfig.PVP_ARENA_DISCIPLINE_REWARD_RATE;
	}
	
	@Override
	public float getChaosRewardRate() {
		return RateConfig.PVP_ARENA_CHAOS_REWARD_RATE;
	}

	@Override
	public float getHarmonyRewardRate() {
		return RateConfig.PVP_ARENA_HARMONY_REWARD_RATE;
	}

	@Override
	public float getGloryRewardRate() {
		return RateConfig.PVP_ARENA_GLORY_REWARD_RATE;
	}

	@Override
	public float getIdgelResearchCenterRewardRate() {
		return RateConfig.IDGEL_RESEARCH_CENTER_INSTANCE_REWARD_RATE;
	}

	@Override
	public float getSellLimitRate() {
		return RateConfig.SELL_LIMIT_RATE;
	}
	
	@Override
	public float getGpNpcRate() {
		return RateConfig.GP_NPC_RATE;
	}
}