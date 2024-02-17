package com.aionemu.gameserver.model.templates.rewards;

/**
 * @author Estrayl
 */
public record ArenaRewardItem(int itemId, int baseCount, int rankingCount, int scoreCount) {

	public int getTotalCount() {
		return baseCount + rankingCount + scoreCount;
	}
}
