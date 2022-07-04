package com.aionemu.gameserver.custom.instance;

/**
 * @author Jo
 */
public enum CustomInstanceRankEnum {

	IRON(0, '\uE02B'),
	BRONZE(1, '\uE02C'),
	SILVER(3, '\uE02D'),
	GOLD(5, '\uE02E'),
	PLATINUM(8, '\uE02F'),
	MITHRIL(12, '\uE030'),
	CERANIUM(16, '\uE0A8'),
	ANCIENT(20, '\uE0AA'),
	ANCIENT_PLUS(24, '\uE0AA');

	private final int minRank;
	private final char rankIcon;

	CustomInstanceRankEnum(int minRank, char rankIcon) {
		this.minRank = minRank;
		this.rankIcon = rankIcon;
	}

	public int getMinRank() {
		return minRank;
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
