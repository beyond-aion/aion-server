package com.aionemu.gameserver.model.templates.spawns.siegespawns;

import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnSpotTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;

/**
 * @author xTz
 */
public class SiegeSpawnTemplate extends SpawnTemplate {

	private final int siegeId;
	private final SiegeRace siegeRace;
	private final SiegeModType siegeModType;

	public SiegeSpawnTemplate(int siegeId, SiegeRace siegeRace, SiegeModType siegeModType, SpawnGroup spawnGroup, SpawnSpotTemplate spot) {
		super(spawnGroup, spot);
		this.siegeId = siegeId;
		this.siegeRace = siegeRace;
		this.siegeModType = siegeModType;
	}

	public SiegeSpawnTemplate(int siegeId, SiegeRace siegeRace, SiegeModType siegeModType, SpawnGroup spawnGroup, float x, float y, float z,
		byte heading, int randWalk, String walkerId, int staticId, int fly) {
		super(spawnGroup, x, y, z, heading, randWalk, walkerId, staticId, fly);
		this.siegeId = siegeId;
		this.siegeRace = siegeRace;
		this.siegeModType = siegeModType;
	}

	public int getSiegeId() {
		return siegeId;
	}

	public SiegeRace getSiegeRace() {
		return siegeRace;
	}

	public SiegeModType getSiegeModType() {
		return siegeModType;
	}
}
