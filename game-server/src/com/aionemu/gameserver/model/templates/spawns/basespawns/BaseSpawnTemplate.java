package com.aionemu.gameserver.model.templates.spawns.basespawns;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup2;
import com.aionemu.gameserver.model.templates.spawns.SpawnSpotTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;

/**
 *
 * @author Source
 */
public class BaseSpawnTemplate extends SpawnTemplate {

	private int id;
	private Race baseRace;

	public BaseSpawnTemplate(SpawnGroup2 spawnGroup, SpawnSpotTemplate spot) {
		super(spawnGroup, spot);
	}

	public BaseSpawnTemplate(SpawnGroup2 spawnGroup, float x, float y, float z, byte heading, int randWalk, String walkerId,
			int staticId, int fly) {
		super(spawnGroup, x, y, z, heading, randWalk, walkerId, staticId, fly);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Race getBaseRace() {
		return baseRace;
	}

	public void setBaseRace(Race baseRace) {
		this.baseRace = baseRace;
	}

}