package com.aionemu.gameserver.model.templates.spawns.riftspawns;

import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnSpotTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;

/**
 * @author Source
 */
public class RiftSpawnTemplate extends SpawnTemplate {

	private int id;

	public RiftSpawnTemplate(SpawnGroup spawnGroup, SpawnSpotTemplate spot) {
		super(spawnGroup, spot);
	}

	public RiftSpawnTemplate(SpawnGroup spawnGroup, float x, float y, float z, byte heading, int randWalk, String walkerId, int staticId, int fly) {
		super(spawnGroup, x, y, z, heading, randWalk, walkerId, staticId, fly);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
