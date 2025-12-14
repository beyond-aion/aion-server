package com.aionemu.gameserver.model.templates.spawns;

import java.util.*;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.base.BaseOccupier;
import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.templates.event.EventTemplate;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.panesterra.AhserionsFlightSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.riftspawns.RiftSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.vortexspawns.VortexSpawnTemplate;
import com.aionemu.gameserver.model.vortex.VortexStateType;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraFaction;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;

/**
 * @author xTz, Rolandas
 */
public class SpawnGroup {

	private final int worldId;
	private final int npcId;
	private final int pool;
	private final int respawnTime;
	private final byte difficultId;
	private final SpawnHandlerType handlerType;
	private final TemporarySpawn temporarySpawn;
	private final List<SpawnTemplate> spots;
	private final Map<Integer, Set<SpawnTemplate>> poolUsedTemplates;
	private final EventTemplate eventTemplate;

	public SpawnGroup(int worldId, int npcId, int respawnTime, EventTemplate eventTemplate) {
		this(worldId, npcId, 0, respawnTime, (byte) 0, null, null, new ArrayList<>(1), eventTemplate);
	}

	public SpawnGroup(int worldId, Spawn spawn) {
		this(worldId, spawn, new ArrayList<>(spawn.getSpawnSpotTemplates().size()));
		for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
			SpawnTemplate spawnTemplate = new SpawnTemplate(this, template);
			spots.add(spawnTemplate);
		}
	}

	public SpawnGroup(int worldId, Spawn spawn, int id, BaseOccupier occupier) {
		this(worldId, spawn, new ArrayList<>(spawn.getSpawnSpotTemplates().size()));
		for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
			BaseSpawnTemplate spawnTemplate = new BaseSpawnTemplate(this, template);
			spawnTemplate.setId(id);
			spawnTemplate.setOccupier(occupier);
			spots.add(spawnTemplate);
		}
	}

	public SpawnGroup(int worldId, Spawn spawn, int id) {
		this(worldId, spawn, new ArrayList<>(spawn.getSpawnSpotTemplates().size()));
		for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
			RiftSpawnTemplate spawnTemplate = new RiftSpawnTemplate(this, template);
			spawnTemplate.setId(id);
			spots.add(spawnTemplate);
		}
	}

	public SpawnGroup(int worldId, Spawn spawn, int id, VortexStateType type) {
		this(worldId, spawn, new ArrayList<>(spawn.getSpawnSpotTemplates().size()));
		for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
			VortexSpawnTemplate spawnTemplate = new VortexSpawnTemplate(this, template);
			spawnTemplate.setId(id);
			spawnTemplate.setStateType(type);
			spots.add(spawnTemplate);
		}
	}

	public SpawnGroup(int worldId, Spawn spawn, int siegeId, SiegeRace race, SiegeModType mod) {
		this(worldId, spawn, new ArrayList<>(spawn.getSpawnSpotTemplates().size()));
		for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
			SiegeSpawnTemplate spawnTemplate = new SiegeSpawnTemplate(siegeId, race, mod, this, template);
			spots.add(spawnTemplate);
		}
	}

	/**
	 * For Ahserion's Flight
	 */
	public SpawnGroup(int worldId, Spawn spawn, int stage, PanesterraFaction faction) {
		this(worldId, spawn, new ArrayList<>(spawn.getSpawnSpotTemplates().size()));
		for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
			AhserionsFlightSpawnTemplate ahserionTemplate = new AhserionsFlightSpawnTemplate(this, template);
			ahserionTemplate.setStage(stage);
			ahserionTemplate.setPanesterraTeam(faction);
			spots.add(ahserionTemplate);
		}
	}

	private SpawnGroup(int worldId, Spawn spawn, List<SpawnTemplate> spots) {
		this(worldId, spawn.getNpcId(), spawn.getPool(), spawn.getRespawnTime(), spawn.getDifficultId(), spawn.getSpawnHandlerType(), spawn.getTemporarySpawn(), spots, spawn.getEventTemplate());
	}

	private SpawnGroup(int worldId, int npcId, int pool, int respawnTime, byte difficultId, SpawnHandlerType handlerType, TemporarySpawn temporarySpawn,
		List<SpawnTemplate> spots, EventTemplate eventTemplate) {
		this.worldId = worldId;
		this.npcId = npcId;
		this.pool = pool;
		this.respawnTime = respawnTime;
		this.difficultId = difficultId;
		this.handlerType = handlerType;
		this.temporarySpawn = temporarySpawn;
		this.spots = spots;
		this.poolUsedTemplates = hasPool() ? new HashMap<>() : Collections.emptyMap();
		this.eventTemplate = eventTemplate;
	}

	public List<SpawnTemplate> getSpawnTemplates() {
		return spots;
	}

	public void addSpawnTemplate(SpawnTemplate spawnTemplate) {
		synchronized (spots) {
			spots.add(spawnTemplate);
		}
	}

	public int getWorldId() {
		return worldId;
	}

	public int getNpcId() {
		return npcId;
	}

	public TemporarySpawn getTemporarySpawn() {
		return temporarySpawn;
	}

	public int getPool() {
		return pool;
	}

	public boolean hasPool() {
		return pool > 0;
	}

	public byte getDifficultId() {
		return difficultId;
	}

	public int getRespawnTime() {
		return respawnTime;
	}

	public boolean isTemporarySpawn() {
		return temporarySpawn != null;
	}

	public SpawnHandlerType getHandlerType() {
		return handlerType;
	}

	public SpawnTemplate reserveRandomFreePoolSpot(int instanceId) {
		synchronized (poolUsedTemplates) {
			Set<SpawnTemplate> occupiedSpots = poolUsedTemplates.computeIfAbsent(instanceId, _ -> new HashSet<>(pool));
			SpawnTemplate freeSpot = Rnd.get(spots.stream().filter(spot -> !occupiedSpots.contains(spot)).toList());
			if (freeSpot == null) {
				LoggerFactory.getLogger(SpawnGroup.class).warn("All spots are used, could not get random spot for npcId: " + npcId + ", worldId: " + worldId);
				return null;
			}
			occupiedSpots.add(freeSpot);
			return freeSpot;
		}
	}

	public void resetPoolSpot(int instanceId, SpawnTemplate template) {
		synchronized (poolUsedTemplates) {
			poolUsedTemplates.getOrDefault(instanceId, Collections.emptySet()).remove(template);
		}
	}

	/**
	 * Call it before each randomization to unset all template use.
	 */
	public void resetPoolSpots(int instanceId) {
		synchronized (poolUsedTemplates) {
			poolUsedTemplates.getOrDefault(instanceId, Collections.emptySet()).clear();
		}
	}

	public EventTemplate getEventTemplate() {
		return eventTemplate;
	}
}
