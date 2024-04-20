package com.aionemu.gameserver.utils.stats;

import java.util.NoSuchElementException;

public enum DropRewardEnum {
	MINUS_10(-10, 0),
	MINUS_9(-9, 40),
	MINUS_8(-8, 60),
	MINUS_7(-7, 70),
	MINUS_6(-6, 80),
	MINUS_5(-5, 100);

	private final int dropRewardPercent;
	private final int levelDifference;

	DropRewardEnum(int levelDifference, int dropRewardPercent) {
		this.levelDifference = levelDifference;
		this.dropRewardPercent = dropRewardPercent;
	}

	public int rewardPercent() {
		return dropRewardPercent;
	}

	/**
	 * @param levelDifference
	 *          between two objects
	 * @return Drop reward percentage
	 */
	public static int dropRewardFrom(int levelDifference) {
		if (levelDifference <= MINUS_10.levelDifference)
			return MINUS_10.dropRewardPercent;
		else if (levelDifference >= MINUS_5.levelDifference)
			return MINUS_5.dropRewardPercent;

		for (DropRewardEnum dropReward : values()) {
			if (dropReward.levelDifference == levelDifference) {
				return dropReward.dropRewardPercent;
			}
		}

		throw new NoSuchElementException("Drop reward for such level difference was not found");
	}
}
