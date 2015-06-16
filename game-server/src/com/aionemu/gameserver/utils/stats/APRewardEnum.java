package com.aionemu.gameserver.utils.stats;

import java.util.NoSuchElementException;

/**
 * @author Source
 */
public enum APRewardEnum {

	GRADE9_SOLDIER(1, 100f),
	GRADE8_SOLDIER(2, 100f),
	GRADE7_SOLDIER(3, 100f),
	GRADE6_SOLDIER(4, 93.75f),
	GRADE5_SOLDIER(5, 87.5f),
	GRADE4_SOLDIER(6, 84.75f),
	GRADE3_SOLDIER(7, 81.25f),
	GRADE2_SOLDIER(8, 62.5f),
	GRADE1_SOLDIER(9, 37.5f),
	STAR1_OFFICER(10, 31.25f),
	STAR2_OFFICER(11, 31.25f),
	STAR3_OFFICER(12, 18.75f),
	STAR4_OFFICER(13, 18.75f),
	STAR5_OFFICER(14, 12.5f),
	GENERAL(15, 6.25f),
	GREAT_GENERAL(16, 6.25f),
	COMMANDER(17, 6.25f),
	SUPREME_COMMANDER(18, 6.25f);

	private int playerRank;

	private float rewardPercent;

	private APRewardEnum(int playerRank, float rewardPercent) {
		this.playerRank = playerRank;
		this.rewardPercent = rewardPercent;
	}

	public float rewardPercent() {
		return rewardPercent;
	}

	/**
	 * @param playerRank current Abyss Rank
	 * @return AP reward percentage
	 */
	public static float apReward(int playerRank) {
		if (playerRank < GRADE9_SOLDIER.playerRank) {
			return GRADE9_SOLDIER.rewardPercent;
		}
		if (playerRank > SUPREME_COMMANDER.playerRank) {
			return SUPREME_COMMANDER.rewardPercent;
		}

		for (APRewardEnum apReward : values()) {
			if (apReward.playerRank == playerRank) {
				return apReward.rewardPercent;
			}
		}

		throw new NoSuchElementException("AP reward for such rank was not found");
	}

}