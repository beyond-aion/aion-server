package com.aionemu.gameserver.model.siege;


/**
 * @author ViAl
 *
 */
public enum GloryPointsRewards {
	
	/**
	 * KATALAM
	 */
	SILLUS_1(5011, 1, 10, 300, 150),
	SILLUS_2(5011, 2, 30, 250, 125),
	SILLUS_3(5011, 3, 50, 200, 100),
	SILLUS_4(5011, 4, 200, 100, 50),
	SILONA_1(6011, 1, 10, 300, 150),
	SILONA_2(6011, 2, 30, 250, 125),
	SILONA_3(6011, 3, 50, 200, 100),
	SILONA_4(6011, 4, 200, 100, 50),
	PRADETH_1(6021, 1, 10, 300, 150),
	PRADETH_2(6021, 2, 30, 250, 125),
	PRADETH_3(6021, 3, 50, 200, 100),
	PRADETH_4(6021, 4, 200, 100, 50),
	/**
	 * ABYSS
	 */
	//DIVINE_1(1011, 1, 10, 200, 100),
	//DIVINE_2(1011, 2, 30, 150, 75),
	//DIVINE_3(1011, 3, 50, 150, 75),
	//DIVINE_4(1011, 4, 200, 100, 50),
	//ROAH_1(1211, 1, 10, 200, 100),
	//ROAH_2(1211, 2, 30, 150, 75),
	//ROAH_3(1211, 3, 50, 150, 75),
	//ROAH_4(1211, 4, 200, 100, 50),
	KROTAN_1(1221, 1, 10, 200, 100),
	KROTAN_2(1221, 2, 30, 150, 75),
	KROTAN_3(1221, 3, 50, 150, 75),
	KROTAN_4(1221, 4, 200, 100, 50),
	KYSIS_1(1231, 1, 10, 200, 100),
	KYSIS_2(1231, 2, 30, 150, 75),
	KYSIS_3(1231, 3, 50, 150, 75),
	KYSIS_4(1231, 4, 200, 100, 50),
	MIREN_1(1241, 1, 10, 200, 100),
	MIREN_2(1241, 2, 30, 150, 75),
	MIREN_3(1241, 3, 50, 150, 75),
	MIREN_4(1241, 4, 200, 100, 50);
	//ASTERIA_1(1251, 1, 10, 200, 100),
	//ASTERIA_2(1251, 2, 30, 150, 75),
	//ASTERIA_3(1251, 3, 50, 150, 75),
	//ASTERIA_4(1251, 4, 200, 100, 50);
	
	private int siegeId;
	private int winPlace;
	private int playersCount;
	private int gpForWin;
	private int gpForLost;
	
	/**
	 * @param siegeId
	 * @param winPlace
	 * @param gpForWin
	 * @param gpForLost
	 */
	private GloryPointsRewards(int siegeId, int winPlace, int playersCount, int gpForWin, int gpForLost) {
		this.siegeId = siegeId;
		this.winPlace = winPlace;
		this.playersCount = playersCount;
		this.gpForWin = gpForWin;
		this.gpForLost = gpForLost;
	}
	
	public int getSiegeId() {
		return siegeId;
	}

	public int getWinPlace() {
		return winPlace;
	}
	
	public int getPlayersCount() {
		return playersCount;
	}

	public int getGpForWin() {
		return gpForWin;
	}

	public int getGpForLost() {
		return gpForLost;
	}

	public static boolean hasRewardForSiege(int siegeId) {
		for(GloryPointsRewards reward : values()) {
			if(reward.getSiegeId() == siegeId)
				return true;
		}
		return false;
	}
	
	public static GloryPointsRewards getReward(int siegeId, int winPlace) {
		for(GloryPointsRewards reward : values()) {
			if(reward.getSiegeId() == siegeId && reward.getWinPlace() == winPlace)
				return reward;
		}
		return null;
	}
}
