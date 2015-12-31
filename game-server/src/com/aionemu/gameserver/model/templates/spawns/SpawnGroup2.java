package com.aionemu.gameserver.model.templates.spawns;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.taskmanager.AbstractLockManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.panesterra.AhserionsFlightSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.riftspawns.RiftSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.vortexspawns.VortexSpawnTemplate;
import com.aionemu.gameserver.model.vortex.VortexStateType;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraTeamId;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;

import javolution.util.FastTable;

/**
 * @author xTz
 * @modified Rolandas
 */
public class SpawnGroup2 extends AbstractLockManager {

	private static final Logger log = LoggerFactory.getLogger(SpawnGroup2.class);

	private int worldId;
	private int npcId;
	private int pool;
	private byte difficultId;
	private TemporarySpawn temporarySpawn;
	private int respawnTime;
	private SpawnHandlerType handlerType;
	private List<SpawnTemplate> spots = new FastTable<SpawnTemplate>();
	private HashMap<Integer, HashMap<SpawnTemplate, Boolean>> poolUsedTemplates;

	public SpawnGroup2(int worldId, Spawn spawn) {
		this.worldId = worldId;
		initializing(spawn);
		for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
			SpawnTemplate spawnTemplate = new SpawnTemplate(this, template);
			if (spawn.isEventSpawn())
				spawnTemplate.setEventTemplate(spawn.getEventTemplate());
			spots.add(spawnTemplate);
		}
	}

	public SpawnGroup2(int worldId, Spawn spawn, int id, Race race) {
		this.worldId = worldId;
		initializing(spawn);
		for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
			BaseSpawnTemplate spawnTemplate = new BaseSpawnTemplate(this, template);
			spawnTemplate.setId(id);
			spawnTemplate.setBaseRace(race);
			spots.add(spawnTemplate);
		}
	}

	public SpawnGroup2(int worldId, Spawn spawn, int id) {
		this.worldId = worldId;
		initializing(spawn);
		for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
			RiftSpawnTemplate spawnTemplate = new RiftSpawnTemplate(this, template);
			spawnTemplate.setId(id);
			spots.add(spawnTemplate);
		}
	}

	public SpawnGroup2(int worldId, Spawn spawn, int id, VortexStateType type) {
		this.worldId = worldId;
		initializing(spawn);
		for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
			VortexSpawnTemplate spawnTemplate = new VortexSpawnTemplate(this, template);
			spawnTemplate.setId(id);
			spawnTemplate.setStateType(type);
			spots.add(spawnTemplate);
		}
	}

	public SpawnGroup2(int worldId, Spawn spawn, int siegeId, SiegeRace race, SiegeModType mod) {
		this.worldId = worldId;
		initializing(spawn);
		for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
			SiegeSpawnTemplate spawnTemplate = new SiegeSpawnTemplate(this, template);
			spawnTemplate.setSiegeId(siegeId);
			spawnTemplate.setSiegeRace(race);
			spawnTemplate.setSiegeModType(mod);
			spots.add(spawnTemplate);
		}
	}
	
	/**
	 * For Ahserions Flight
	 */
	public SpawnGroup2(int worldId, Spawn spawn, int stage, PanesterraTeamId team) {
		this.worldId = worldId;
		initializing(spawn);
		for (SpawnSpotTemplate template : spawn.getSpawnSpotTemplates()) {
			AhserionsFlightSpawnTemplate ahserionTemplate = new AhserionsFlightSpawnTemplate(this, template);
			ahserionTemplate.setStage(stage);
			ahserionTemplate.setPanesterraTeam(team);
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
			poolUsedTemplates = new HashMap<Integer, HashMap<SpawnTemplate, Boolean>>();
	}

	public SpawnGroup2(int worldId, int npcId) {
		this.worldId = worldId;
		this.npcId = npcId;
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

	public TemporarySpawn geTemporarySpawn() {
		return temporarySpawn;
	}

	public int getPool() {
		return pool;
	}

	public boolean hasPool() {
		return pool > 0;
	}

	public int getRespawnTime() {
		return respawnTime;
	}

	public void setRespawnTime(int respawnTime) {
		this.respawnTime = respawnTime;
	}

	public boolean isTemporarySpawn() {
		return temporarySpawn != null;
	}

	public SpawnHandlerType getHandlerType() {
		return handlerType;
	}

	public SpawnTemplate getRndTemplate(int instanceId) {
		final List<SpawnTemplate> allTemplates = spots;
		List<SpawnTemplate> templates = new FastTable<SpawnTemplate>();
		super.readLock();
		try {
			for (SpawnTemplate template : allTemplates) {
				if (!isTemplateUsed(instanceId, template)) {
					templates.add(template);
				}
			}
			if (templates.size() == 0) {
				log.warn("Pool size more then spots, npcId: " + npcId + ", worldId: " + worldId);
				return null;
			}
		} finally {
			super.readUnlock();
		}
		SpawnTemplate spawnTemplate = templates.get(Rnd.get(0, templates.size() - 1));
		setTemplateUse(instanceId, spawnTemplate, true);
		return spawnTemplate;
	}

	public void setTemplateUse(int instanceId, SpawnTemplate template, boolean isUsed) {
		super.writeLock();
		try {
			HashMap<SpawnTemplate, Boolean> states = poolUsedTemplates.get(instanceId);
			if (states == null) {
				states = new HashMap<SpawnTemplate, Boolean>();
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
	 * 
	 * @param instanceId
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

	public byte getDifficultId() {
		return difficultId;
	}

}
