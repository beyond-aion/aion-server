package com.aionemu.gameserver.model.templates.spawns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.taskmanager.AbstractLockManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.Race;
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
public class SpawnGroup extends AbstractLockManager {

	private int worldId;
	private int npcId;
	private int pool;
	private byte difficultId;
	private TemporarySpawn temporarySpawn;
	private int respawnTime;
	private SpawnHandlerType handlerType;
	private List<SpawnTemplate> spots;
	private HashMap<Integer, HashMap<SpawnTemplate, Boolean>> poolUsedTemplates;
	private EventTemplate eventTemplate;

	public SpawnGroup(int worldId, int npcId, int respawnTime) {
		this.worldId = worldId;
		this.npcId = npcId;
		this.respawnTime = respawnTime;
		this.spots = new ArrayList<>(1);
	}

	public SpawnGroup(int worldId, Spawn spawn) {
		this.worldId = worldId;
		initializing(spawn);
		for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
			SpawnTemplate spawnTemplate = new SpawnTemplate(this, template);
			spots.add(spawnTemplate);
		}
	}

	public SpawnGroup(int worldId, Spawn spawn, int id, Race race) {
		this.worldId = worldId;
		initializing(spawn);
		for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
			BaseSpawnTemplate spawnTemplate = new BaseSpawnTemplate(this, template);
			spawnTemplate.setId(id);
			spawnTemplate.setBaseRace(race);
			spots.add(spawnTemplate);
		}
	}

	public SpawnGroup(int worldId, Spawn spawn, int id) {
		this.worldId = worldId;
		initializing(spawn);
		for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
			RiftSpawnTemplate spawnTemplate = new RiftSpawnTemplate(this, template);
			spawnTemplate.setId(id);
			spots.add(spawnTemplate);
		}
	}

	public SpawnGroup(int worldId, Spawn spawn, int id, VortexStateType type) {
		this.worldId = worldId;
		initializing(spawn);
		for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
			VortexSpawnTemplate spawnTemplate = new VortexSpawnTemplate(this, template);
			spawnTemplate.setId(id);
			spawnTemplate.setStateType(type);
			spots.add(spawnTemplate);
		}
	}

	public SpawnGroup(int worldId, Spawn spawn, int siegeId, SiegeRace race, SiegeModType mod) {
		this.worldId = worldId;
		initializing(spawn);
		for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
			SiegeSpawnTemplate spawnTemplate = new SiegeSpawnTemplate(siegeId, race, mod, this, template);
			spots.add(spawnTemplate);
		}
	}

	/**
	 * For Ahserion's Flight
	 */
	public SpawnGroup(int worldId, Spawn spawn, int stage, PanesterraFaction faction) {
		this.worldId = worldId;
		initializing(spawn);
		for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
			AhserionsFlightSpawnTemplate ahserionTemplate = new AhserionsFlightSpawnTemplate(this, template);
			ahserionTemplate.setStage(stage);
			ahserionTemplate.setPanesterraTeam(faction);
			spots.add(ahserionTemplate);
		}
	}

	private void initializing(Spawn spawn) {
		temporarySpawn = spawn.getTemporarySpawn();
		respawnTime = spawn.getRespawnTime();
		pool = spawn.getPool();
		npcId = spawn.getNpcId();
		handlerType = spawn.getSpawnHandlerType();
		difficultId = spawn.getDifficultId();
		if (hasPool())
			poolUsedTemplates = new HashMap<>();
		spots = new ArrayList<>(spawn.getSpawnSpotTemplates().size());
		if (spawn.isEventSpawn())
			eventTemplate = spawn.getEventTemplate();
	}

	public List<SpawnTemplate> getSpawnTemplates() {
		return spots;
	}

	public void addSpawnTemplate(SpawnTemplate spawnTemplate) {
		super.writeLock();
		try {
			spots.add(spawnTemplate);
		} finally {
			super.writeUnlock();
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

	public SpawnTemplate getRndTemplate(int instanceId) {
		final List<SpawnTemplate> allTemplates = spots;
		List<SpawnTemplate> templates = new ArrayList<>();
		super.readLock();
		try {
			for (SpawnTemplate template : allTemplates) {
				if (!isTemplateUsed(instanceId, template)) {
					templates.add(template);
				}
			}
		} finally {
			super.readUnlock();
		}
		if (templates.isEmpty()) {
			LoggerFactory.getLogger(SpawnGroup.class).warn("All spots are used, could not get random spot for npcId: " + npcId + ", worldId: " + worldId);
			return null;
		}
		SpawnTemplate spawnTemplate = Rnd.get(templates);
		setTemplateUse(instanceId, spawnTemplate, true);
		return spawnTemplate;
	}

	public void setTemplateUse(int instanceId, SpawnTemplate template, boolean isUsed) {
		super.writeLock();
		try {
			HashMap<SpawnTemplate, Boolean> states = poolUsedTemplates.get(instanceId);
			if (states == null) {
				states = new HashMap<>();
				poolUsedTemplates.put(instanceId, states);
			}
			states.put(template, isUsed);
		} finally {
			super.writeUnlock();
		}
	}

	public boolean isTemplateUsed(int instanceId, SpawnTemplate template) {
		super.readLock();
		try {
			HashMap<SpawnTemplate, Boolean> states = poolUsedTemplates.get(instanceId);
			if (states == null)
				return false;
			Boolean state = states.get(template);
			if (state == null)
				return false;
			return state;
		} finally {
			super.readUnlock();
		}
	}

	/**
	 * Call it before each randomization to unset all template use.
	 */
	public void resetTemplates(int instanceId) {
		HashMap<SpawnTemplate, Boolean> states = poolUsedTemplates.get(instanceId);
		if (states == null)
			return;
		super.writeLock();
		try {
			for (SpawnTemplate template : states.keySet()) {
				states.put(template, false);
			}
		} finally {
			super.writeUnlock();
		}
	}

	public EventTemplate getEventTemplate() {
		return eventTemplate;
	}
}
