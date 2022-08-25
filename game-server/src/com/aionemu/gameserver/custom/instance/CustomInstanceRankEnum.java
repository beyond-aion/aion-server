package com.aionemu.gameserver.custom.instance;

/**
 * @author Jo
 */
public enum CustomInstanceRankEnum {

	IRON(0, 4, '\uE02B'),
	BRONZE(1, 10,'\uE02C'),
	SILVER(3, 16,'\uE02D'),
	GOLD(5, 22,'\uE02E'),
	PLATINUM(8, 28,'\uE02F'),
	MITHRIL(12, 34,'\uE030'),
	CERANIUM(16, 40,'\uE0A8'),
	ANCIENT(20, 46,'\uE0AA'),
	ANCIENT_PLUS(24, 50,'\uE0AA');

	private final int minRank;
	private final int minReward;
	private final char rankIcon;

	CustomInstanceRankEnum(int minRank, int minReward, char rankIcon) {
		this.minRank = minRank;
		this.minReward = minReward;
		this.rankIcon = rankIcon;
	}

	public int getMinRank() {
		return minRank;
	}

	public int getMinReward() {
		return minReward;
	}

	public char getRankIcon() {
		return rankIcon;
	}

	public static String getRankDescription(int value) {
		CustomInstanceRankEnum rank = getByRank(value);
		return rank.rankIcon + " " + (rank == ANCIENT_PLUS ? "ANCIENT +" + (value + 1 - rank.minRank) : rank.name());
	}

	public static CustomInstanceRankEnum getByRank(int rank) {
		for (int i = values().length - 1; i >= 0; i--) {
			if (values()[i].minRank <= rank)
				return values()[i];
		}
		throw new IllegalArgumentException(rank + " is no valid rank");
	}
}
