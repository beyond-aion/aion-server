package com.aionemu.gameserver.utils.stats;

import java.util.NoSuchElementException;

/**
 * @author ?
 * @modified Neon
 */
public enum DropRewardEnum {
	MINUS_10(-10, 0),
	MINUS_9(-9, 40),
	MINUS_8(-8, 60),
	MINUS_7(-7, 70),
	MINUS_6(-6, 80),
	MINUS_5(-5, 90),
	MINUS_4(-4, 100);

	private int dropRewardPercent;
	private int levelDifference;

	private DropRewardEnum(int levelDifference, int dropRewardPercent) {
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
		else if (levelDifference >= MINUS_4.levelDifference)
			return MINUS_4.dropRewardPercent;

		for (DropRewardEnum dropReward : values()) {
			if (dropReward.levelDifference == levelDifference) {
				return dropReward.dropRewardPercent;
			}
		}

		throw new NoSuchElementException("Drop reward for such level difference was not found");
	}
}
