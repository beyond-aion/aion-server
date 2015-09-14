package com.aionemu.gameserver.model.templates.spawns;

/**
 * @author Rolandas
 */
public final class SpawnSearchResult {

	private SpawnSpotTemplate spot;
	private int worldId;

	public SpawnSearchResult(int worldId, SpawnSpotTemplate spot) {
		this.worldId = worldId;
		this.spot = spot;
	}

	public SpawnSpotTemplate getSpot() {
		return spot;
	}

	public int getWorldId() {
		return worldId;
	}
}
