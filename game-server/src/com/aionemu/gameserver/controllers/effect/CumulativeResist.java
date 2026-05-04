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

	float getDurationMultiplier(CumulativeResistType type) {
		return switch (level) {
			case 0, 1 -> 1;
			case 2 -> 0.9f;
			case 3 -> 0.85f;
			case 4 -> 0.8f;
			default -> 0;
		};
	}

	int getCooldownTimeOffset(CumulativeResistType type) {
		return switch (type) {
			case SLEEP, PARALYZE -> 0;
			case FEAR -> 2000;
		};
	}

	int getResistance() {
		resetIfExpired();
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
