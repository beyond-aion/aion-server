package com.aionemu.gameserver.custom.instance;

/**
 * @author Jo
 */
public enum CustomInstanceRankEnum {

	IRON(0),
	BRONZE(3),
	SILVER(6),
	GOLD(9),
	PLATINUM(12),
	MITHRIL(15),
	CERANIUM(18),
	ANCIENT(21),
	ANCIENT_PLUS(24);

	private final int value;

	public int getValue() {
		return value;
	}

	private CustomInstanceRankEnum(int value) {
		this.value = value;
	}

	public static String getRankDescription(int value) {
		if (value >= 24)
			return "ANCIENT +" + (value - 23);
		return values()[value / 3].name();
	}
}
