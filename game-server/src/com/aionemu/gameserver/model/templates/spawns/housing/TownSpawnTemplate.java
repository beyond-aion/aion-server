package com.aionemu.gameserver.model.templates.spawns.housing;

import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnSpotTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;

public class TownSpawnTemplate extends SpawnTemplate {

	private final int townId;

	public TownSpawnTemplate(SpawnGroup spawnGroup, SpawnSpotTemplate spot, int townId) {
		super(spawnGroup, spot);
		this.townId = townId;
	}

	public int getTownId() {
		return townId;
	}
}
