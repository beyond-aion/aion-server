package com.aionemu.gameserver.model.templates.spawns.vortexspawns;

import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnSpotTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.vortex.VortexStateType;

/**
 * @author Source
 */
public class VortexSpawnTemplate extends SpawnTemplate {

	private int id;
	private VortexStateType stateType;

	public VortexSpawnTemplate(SpawnGroup spawnGroup, SpawnSpotTemplate spot) {
		super(spawnGroup, spot);
	}

	public int getId() {
		return id;
	}

	public VortexStateType getStateType() {
		return stateType;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setStateType(VortexStateType stateType) {
		this.stateType = stateType;
	}

	public final boolean isInvasion() {
		return stateType.equals(VortexStateType.INVASION);
	}

	public final boolean isPeace() {
		return stateType.equals(VortexStateType.PEACE);
	}

}
