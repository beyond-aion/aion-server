package com.aionemu.gameserver.model.templates.spawns;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.event.EventTemplate;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;

/**
 * @author xTz, Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Spawn")
public class Spawn {

	@XmlAttribute(name = "npc_id", required = true)
	private int npcId;

	@XmlAttribute(name = "respawn_time")
	private Integer respawnTime = 0;

	@XmlAttribute(name = "pool")
	private Integer pool = 0;

	@XmlAttribute(name = "difficult_id")
	private byte difficultId;

	@XmlAttribute(name = "custom")
	private Boolean isCustom = false;

	@XmlAttribute(name = "handler")
	private SpawnHandlerType handler;

	@XmlElement(name = "temporary_spawn")
	private TemporarySpawn temporaySpawn;

	@XmlElement(name = "spot")
	private List<SpawnSpotTemplate> spawnTemplates;

	@XmlTransient
	private EventTemplate eventTemplate;

	public Spawn() {
	}

	public Spawn(int npcId, int respawnTime, SpawnHandlerType handler) {
		this.npcId = npcId;
		this.respawnTime = respawnTime;
		this.handler = handler;
	}

	void beforeMarshal(Marshaller marshaller) {
		if (pool == 0)
			pool = null;
		if (isCustom == false)
			isCustom = null;
	}

	void afterMarshal(Marshaller marshaller) {
		if (isCustom == null)
			isCustom = false;
		if (pool == null)
			pool = 0;
	}

	public int getNpcId() {
		return npcId;
	}

	public int getPool() {
		return pool;
	}

	public TemporarySpawn getTemporarySpawn() {
		return temporaySpawn;
	}

	public int getRespawnTime() {
		return respawnTime;
	}

	public SpawnHandlerType getSpawnHandlerType() {
		return handler;
	}

	public List<SpawnSpotTemplate> getSpawnSpotTemplates() {
		if (spawnTemplates == null)
			spawnTemplates = new ArrayList<>();
		return spawnTemplates;
	}

	public boolean isCustom() {
		return isCustom != null && isCustom;
	}

	public void setCustom(boolean isCustom) {
		this.isCustom = isCustom;
	}

	public boolean isEventSpawn() {
		return eventTemplate != null;
	}

	public EventTemplate getEventTemplate() {
		return eventTemplate;
	}

	public void setEventTemplate(EventTemplate eventTemplate) {
		this.eventTemplate = eventTemplate;
	}

	public byte getDifficultId() {
		return difficultId;
	}
}
