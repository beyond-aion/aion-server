package com.aionemu.gameserver.model.templates.spawns.basespawns;

import com.aionemu.gameserver.model.base.BaseOccupier;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnSpotTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;

/**
 * @author Source
 */
public class BaseSpawnTemplate extends SpawnTemplate {

	private int id;
	private BaseOccupier occupier;

	public BaseSpawnTemplate(SpawnGroup spawnGroup, SpawnSpotTemplate spot) {
		super(spawnGroup, spot);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public BaseOccupier getOccupier() {
		return occupier;
	}

	public void setOccupier(BaseOccupier occupier) {
		this.occupier = occupier;
	}

}
