package com.aionemu.gameserver.controllers.effect;

class CumulativeResist {

	private int level;
	private long expirationTime;

	void tryIncrementLevel(long maxDurationMillis) {
		resetIfExpired();
		if (level < 5)
			level++;
		this.expirationTime = System.currentTimeMillis() + maxDurationMillis;
	}

	float getDurationMultiplier() {
		//time_value* from repeated_abnormal_status_immune.xml retail file
		return switch (level) {
			case 0, 1 -> 1;
			case 2 -> 0.9f;
			case 3 -> 0.85f;
			case 4 -> 0.8f;
			default -> 0;
		};
	}

	int getCooldownTimeOffset(CumulativeResistType type) {
		//holding_time2 from repeated_abnormal_status_immune.xml retail file
		return switch (type) {
			case SLEEP, PARALYZE -> 0;
			case FEAR -> 2000;
		};
	}

	int getResistance() {
		resetIfExpired();
		//resist_value* from repeated_abnormal_status_immune.xml retail file
		return switch (level) {
			case 0, 1, 2 -> 0;
			case 3 -> 200;
			case 4 -> 400;
			default -> 1000;
		};
	}

	private void resetIfExpired() {
		if (level > 0 && System.currentTimeMillis() > expirationTime)
			level = 0;
	}
}
