package com.aionemu.gameserver.model.siege;

/**
 * @author Estrayl
 */
public class Assaulter {

	private final int npcId;
	private final float spawnCost;
	private final int headingOffset;
	private final int distanceOffset;

	public Assaulter(int npcId, float spawnCost, int headingOffset, int distanceOffset) {
		this.npcId = npcId;
		this.spawnCost = spawnCost;
		this.headingOffset = headingOffset;
		this.distanceOffset = distanceOffset;
	}

	public int getNpcId() {
		return npcId;
	}

	public float getSpawnCost() {
		return spawnCost;
	}

	public int getHeadingOffset() {
		return headingOffset;
	}

	public int getDistanceOffset() {
		return distanceOffset;
	}
}
