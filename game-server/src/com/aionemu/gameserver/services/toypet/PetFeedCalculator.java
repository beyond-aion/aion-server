package com.aionemu.gameserver.services.toypet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.pet.PetFeedResult;
import com.aionemu.gameserver.model.templates.pet.PetRewards;

/**
 * @author SVDNESS
 * @version 4.8 [JDK 25]
 */

public final class PetFeedCalculator {
	static byte ITEM_MAX_LEVEL = 60;
	static final short[] fullCounts;
	static final byte[] itemLevels;
	static final int[][] pointValues;

	static {
		TreeSet<Short> counts = new TreeSet<>();
		for (var flavour : DataManager.PET_FEED_DATA.getPetFlavours()) {
			if (flavour.getFullCount() > 0) {
				counts.add((short) (flavour.getFullCount() & 0xFFFF));
			}
		}
		fullCounts = new short[counts.size()];
		int i = 0;
		for (var count : counts) {
			fullCounts[i++] = count;
		}
		itemLevels = new byte[ITEM_MAX_LEVEL / 5];
		itemLevels[0] = 5;
		for (int j = 1; j < itemLevels.length; j++) {
			itemLevels[j] = (byte) (itemLevels[j - 1] + 5);
		}
		pointValues = new int[itemLevels.length][fullCounts.length];
		calculate();
	}

	private static void calculate() {
		for (byte itemLevel : itemLevels) {
			short level = (short) (itemLevel & 0xFF);
			if (level < 10) continue;
			for (int j = 0; j < fullCounts.length; j++) {
				short count = (short) (fullCounts[j] & 0xFF);
				int finalLevel = (level % 5 == 0) ? level - 1 : level;
				int pointLevel = itemLevels[finalLevel / 5];
				int feedPoints = Math.max(0, pointLevel - 5) / 5 * 8;
				pointValues[finalLevel / 5][j] = getPoints(feedPoints, count);
			}
		}
	}

	static int getPoints(int feedPoints, int maxFeedCount) {
		int points = 0;
		int state = 0;
		int consumed = 0;
		while (consumed < maxFeedCount) {
			boolean needSwitch = false;
			int oldPoints = points;
			if ((state == 0 && consumed > maxFeedCount * 0.5f) || (state == 1 && consumed > maxFeedCount * 0.8f) || (state == 2 && consumed > maxFeedCount * 1.05)) {
				needSwitch = true;
			}
			points += feedPoints;
			if (needSwitch) {
				state++;
				if (state == 1 && consumed <= 0.487f * maxFeedCount || state == 2 && consumed <= 0.78f * maxFeedCount) {
					state--;
					points = oldPoints;
				}
			}
			consumed++;
		}
		return points;
	}

	public static void updatePetFeedProgress(PetFeedProgress progress, int itemLevel, int maxFeedCount) {
		var currHungryLevel = progress.getHungryLevel();
		if (progress.isLovedFeeded()) {
			if (progress.getLovedFoodRemaining() == 0) {
				return;
			}
			progress.setHungryLevel(PetHungryLevel.FULL);
			progress.incrementCount(true);
			return;
		}
		int oldPoints = progress.getTotalPoints();
		boolean needSwitch = false;
		if ((currHungryLevel == PetHungryLevel.HUNGRY && progress.getRegularCount() > maxFeedCount * 0.5f)
				|| (currHungryLevel == PetHungryLevel.CONTENT && progress.getRegularCount() > maxFeedCount * 0.8f)
				|| (currHungryLevel == PetHungryLevel.SEMIFULL && progress.getRegularCount() > maxFeedCount * 1.05)) {
			needSwitch = true;
		} else {
			int finalLevel = itemLevel;
			if (finalLevel % 5 == 0) {
				finalLevel--;
			}
			byte pointLevel = itemLevels[finalLevel / 5];
			byte pointsEarned = (byte) (Math.max(0, pointLevel - 5) / 5 * 8);
			int feedProgress = progress.getTotalPoints() + pointsEarned;
			progress.setTotalPoints(feedProgress);
		}
		if (needSwitch) {
			var nextLevel = progress.getHungryLevel().getNextValue();
			if (nextLevel == PetHungryLevel.CONTENT && progress.getRegularCount() <= 0.487f * maxFeedCount || nextLevel == PetHungryLevel.SEMIFULL
					&& progress.getRegularCount() <= 0.78f * maxFeedCount) {
				progress.setTotalPoints(oldPoints);
			} else {
				progress.setHungryLevel(nextLevel);
			}
		}
		progress.incrementCount(false);
	}

	public static PetFeedResult getReward(int fullCount, PetRewards rewardGroup, PetFeedProgress progress, int playerLevel) {
		if (progress.getHungryLevel() != PetHungryLevel.FULL || rewardGroup.getResults().isEmpty()) {
			return null;
		}
		int pointsIndex = Arrays.binarySearch(fullCounts, (short) fullCount);
		if (pointsIndex < 0) {
			return null;
		}
		if (progress.isLovedFeeded()) {
			if (rewardGroup.getResults().size() == 1) {
				return rewardGroup.getResults().getFirst();
			}
			List<PetFeedResult> validRewards = new ArrayList<>();
			int maxLevel = 0;
			for (var result : rewardGroup.getResults()) {
				int resultLevel = DataManager.ITEM_DATA.getItemTemplate(result.getItem()).getLevel();
				if (resultLevel > playerLevel) {
					continue;
				}
				if (resultLevel > maxLevel) {
					maxLevel = resultLevel;
					validRewards.clear();
				}
				validRewards.add(result);
			}
			return Rnd.get(validRewards);
		}
		int rewardIndex = getRewardIndex(rewardGroup, progress, pointsIndex);
		return rewardGroup.getResults().get(rewardIndex);
	}

	private static int getRewardIndex(PetRewards rewardGroup, PetFeedProgress progress, int pointsIndex) {
		int rewardIndex = 0;
		int totalRewards = rewardGroup.getResults().size();
		for (int row = 1; row < pointValues.length; row++) {
			int[] points = pointValues[row];
			if (points[pointsIndex] <= progress.getTotalPoints()) {
				rewardIndex = Math.round((float) totalRewards / (pointValues.length - 1) * row) - 1;
			}
		}
		if (rewardIndex < 0) {
			rewardIndex = 0;
		} else if (rewardIndex > rewardGroup.getResults().size() - 1) {
			rewardIndex = rewardGroup.getResults().size() - 1;
		}
		return rewardIndex;
	}
}