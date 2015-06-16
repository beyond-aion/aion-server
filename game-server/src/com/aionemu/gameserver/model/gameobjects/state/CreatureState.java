package com.aionemu.gameserver.model.gameobjects.state;

/**
 * @author ATracer, Sweetkr
 */
public enum CreatureState {

	ACTIVE(1), // basic 1
	FLYING(1 << 1), // 2
	FLIGHT_TELEPORT(1 << 1), // 2
	RESTING(1 << 2), // 4
	DEAD(3 << 1), // 6
	CHAIR(3 << 1), // 6
	FLOATING_CORPSE(1 << 3), // 8
	PRIVATE_SHOP(5 << 1), // 10
	LOOTING(3 << 2), // 12
	WEAPON_EQUIPPED(1 << 5), // 32
	WALKING(1 << 6), // 64
	NPC_IDLE(1 << 6), // 64 (for npc)
	POWERSHARD(1 << 7), // 128
	TREATMENT(1 << 8), // 256
	GLIDING(1 << 9); // 512

	/**
	 * Standing, path flying, free flying, riding, sitting, sitting on chair, dead, fly dead, private shop, looting, fly
	 * looting, default
	 */

	private int id;

	private CreatureState(int id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
}
