package com.aionemu.gameserver.model.gameobjects.state;

import com.aionemu.gameserver.model.ActionState;

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
	LOOTING(RESTING.getId() + FLOATING_CORPSE.getId()), // 4 + 8
	ANY_STANCE(ACTIVE.getId() + FLYING.getId() + RESTING.getId() + FLOATING_CORPSE.getId()); // 1 + 2 + 4 + 8 (only one stance at a time)

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

	/**
	 * @return true if the creature just stands there, meaning it is not flying, riding, resting, sitting, dead, looting or running a private store
	 */
	public static boolean isStanding(int state) {
		return (state & ANY_STANCE.id) == ACTIVE.id;
	}

	/**
	 * @param state
	 *          the raw state value of a creature
	 * @return The state naming that creature, meant as a message parameter (for example "You cannot soul-bind an item while %0.")
	 */
	public static ActionState getActionState(int state) {
		if ((state & WEAPON_EQUIPPED.id) != 0)
			return ActionState.COMBAT;
		if ((state & TREATMENT.id) != 0)
			return ActionState.USING_SKILL;
		if ((state & GLIDING.id) != 0)
			return ActionState.GLIDING;
		if ((state & WALK_MODE.id) != 0)
			return ActionState.MOVING;
		return switch (state & ANY_STANCE.id) {
			case 1 -> ActionState.STANDING;
			case 2 -> ActionState.PATH_FLYING;
			case 3 -> ActionState.FREE_FLYING;
			case 4 -> ActionState.RIDING;
			case 5 -> ActionState.RESTING;
			case 6 -> ActionState.SITTING;
			case 7 -> ActionState.DEAD;
			case 8 -> ActionState.FLY_DEAD;
			case 0xB -> ActionState.PERSONAL_SHOP;
			case 0xC -> ActionState.LOOTING;
			case 0xD -> ActionState.FLY_LOOTING;
			default -> ActionState.CURRENT_STATUS;
		};
	}
}
