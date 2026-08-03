package com.aionemu.gameserver.model.summons;

/**
 * Cause of a summon release, defining how long the summon stays alive and whether its master may take the order back.
 *
 * @author xTz
 */
public enum UnsummonType {

	LOGOUT(0, false),
	DISTANCE(0, false),
	COMMAND(3000, true),
	SUMMON_DEATH(0, false),
	MASTER_DEATH(0, false),
	/** Live time ran out, skill order with release flag (summon casts and vanishes), instance script, ... */
	UNSPECIFIED(3000, false),
	PET_ORDER_UNSUMMON_EFFECT(0, false);

	private final int delayMillis;
	private final boolean cancelableByMaster;

	UnsummonType(int delayMillis, boolean cancelableByMaster) {
		this.delayMillis = delayMillis;
		this.cancelableByMaster = cancelableByMaster;
	}

	public int getDelayMillis() {
		return delayMillis;
	}

	public boolean isInstant() {
		return delayMillis == 0;
	}

	public boolean isCancelableByMaster() {
		return cancelableByMaster;
	}
}
