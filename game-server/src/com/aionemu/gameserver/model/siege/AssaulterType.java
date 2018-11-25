package com.aionemu.gameserver.model.siege;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Estrayl
 */
public enum AssaulterType {

	TELEPORT(0f, Collections.emptyList()),
	COMMANDER(0f, Arrays.asList(1.0f, 1.25f, 1.5f, 1.75f, 2.0f)),
	FIGHTER(0.3f, Arrays.asList(0.2f, 0.4f, 0.8f, 1.0f)),
	ASSASSIN(0.1f, Arrays.asList(0.2f, 0.4f, 0.8f, 1.0f)),
	RANGER(0.2f, Arrays.asList(0.4f, 0.8f, 1.6f, 2.0f)),
	WITCH(0.15f, Arrays.asList(0.5f, 1.0f, 2.0f, 2.5f)),
	PRIEST(0.1f, Arrays.asList(0.6f, 1.2f, 2.4f, 3.0f)),
	GUNNER(0.15f, Arrays.asList(0.4f, 0.8f, 1.6f, 2.0f));

	private final float spawnStake;
	private final List<Float> spawnCosts;

	private AssaulterType(float spawnStake, List<Float> spawnCosts) {
		this.spawnStake = spawnStake;
		this.spawnCosts = spawnCosts;
	}

	public float getSpawnStake() {
		return spawnStake;
	}

	public List<Float> getSpawnCosts() {
		return spawnCosts;
	}
}
