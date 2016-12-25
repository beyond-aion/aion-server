package com.aionemu.gameserver.model.gameobjects.state;

/**
 * @author ATracer, Sweetkr
 */
public enum CreatureState {

	ACTIVE(1), // 1
	FLYING(1 << 1), // 2
	RESTING(1 << 2), // 4
	FLOATING_CORPSE(1 << 3), // 8
	UNK(1 << 4), // 16
	WEAPON_EQUIPPED(1 << 5), // 32
	WALK_MODE(1 << 6), // 64 (set = walking, unset = running)
	POWERSHARD(1 << 7), // 128
	TREATMENT(1 << 8), // 256
	GLIDING(1 << 9), // 512

	// multibit (id = combined value of multiple single-bit states)
	CHAIR(FLYING.getId() + RESTING.getId(), true), // 2 + 4 (need to stand near a chair, otherwise shows resting state)
	DEAD(ACTIVE.getId() + FLYING.getId() + RESTING.getId()), // 1 + 2 + 4
	PRIVATE_SHOP(ACTIVE.getId() + FLYING.getId() + FLOATING_CORPSE.getId(), true), // 1 + 2 + 8
	LOOTING(RESTING.getId() + FLOATING_CORPSE.getId()); // 4 + 8

	private int id;
	private boolean mustMatchExact;

	private CreatureState(int id) {
		this(id, false);
	}

	private CreatureState(int id, boolean mustMatchExact) {
		this.id = id;
		this.mustMatchExact = mustMatchExact;
	}

	public int getId() {
		return id;
	}

	public boolean mustMatchExact() {
		return mustMatchExact;
	}
}
