package com.aionemu.gameserver.utils.rates;

/**
 * @author ATracer
 */
public abstract class Rates {

	public abstract float getGroupXpRate();

	public abstract float getXpRate();

	public abstract float getApNpcRate();

	public abstract float getApPlayerGainRate();

	public abstract float getGpNpcRate();

	public abstract float getGpPlayerGainRate();

	public abstract float getXpPlayerGainRate();
	
	public abstract float getApPlayerLossRate();

	public abstract float getGatheringXPRate();

	public abstract int getGatheringCountRate();

	public abstract float getCraftingXPRate();

	public abstract float getDropRate();

	public abstract float getQuestXpRate();

	public abstract float getQuestKinahRate();

	public abstract float getQuestApRate();

	public abstract float getDpNpcRate();

	public abstract float getDpPlayerRate();

	public abstract int getCraftCritRate();
	
	public abstract int getComboCritRate();

	public abstract float getDisciplineRewardRate();

	public abstract float getChaosRewardRate();

	public abstract float getHarmonyRewardRate();

	public abstract float getIdgelResearchCenterRewardRate();

	public abstract float getGloryRewardRate();

	public abstract float getSellLimitRate();

	/**
	 * @param membership
	 * @return Rates
	 */
	public static Rates getRatesFor(byte membership) {
		switch (membership) {
			case 0:
				return new RegularRates();
			case 1:
				return new PremiumRates();
			case 2:
				return new VipRates();
			default:
				return new VipRates();
		}
	}

}