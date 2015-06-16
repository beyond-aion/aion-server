package com.aionemu.gameserver.utils.stats;

/**
 * @author ATracer, Jangan
 */
public enum XPLossEnum {
	LEVEL_6(6, 1.0),
	LEVEL_30(30, 1.0),
	LEVEL_40(40, 0.35),
	LEVEL_50(50, 0.25),
	LEVEL_55(55, 0.25),
	LEVEL_60(60, 0.25),
	LEVEL_65(65, 0.25);

	private int level;
	private double param;

	private XPLossEnum(int level, double param) {
		this.level = level;
		this.param = param;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @return the param
	 */
	public double getParam() {
		return param;
	}

	/**
	 * @param level
	 * @param expNeed
	 * @return long
	 */
	public static long getExpLoss(int level, long expNeed) {
		if (level < 6)
			return 0;

		for (XPLossEnum xpLossEnum : values()) {
			if (level <= xpLossEnum.getLevel())
				return Math.round(expNeed / 100 * xpLossEnum.getParam());
		}
		return 0;
	}

}
