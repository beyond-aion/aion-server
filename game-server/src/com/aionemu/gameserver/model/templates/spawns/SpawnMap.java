package com.aionemu.gameserver.model.templates.spawns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawn;
import com.aionemu.gameserver.model.templates.spawns.mercenaries.MercenarySpawn;
import com.aionemu.gameserver.model.templates.spawns.panesterra.AhserionsFlightSpawn;
import com.aionemu.gameserver.model.templates.spawns.riftspawns.RiftSpawn;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawn;
import com.aionemu.gameserver.model.templates.spawns.vortexspawns.VortexSpawn;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SpawnMap")
public class SpawnMap {

	@XmlElement(name = "spawn")
	private List<Spawn> spawns;
	@XmlElement(name = "base_spawn")
	private List<BaseSpawn> baseSpawns;
	@XmlElement(name = "rift_spawn")
	private List<RiftSpawn> riftSpawns;
	@XmlElement(name = "siege_spawn")
	private List<SiegeSpawn> siegeSpawns;
	@XmlElement(name = "mercenary_spawn")
	private List<MercenarySpawn> mercenarySpawns;
	@XmlElement(name = "vortex_spawn")
	private List<VortexSpawn> vortexSpawns;
	@XmlElement(name = "ahserion_spawn")
	private List<AhserionsFlightSpawn> ahserionSpawns;
	@XmlAttribute(name = "map_id")
	private int mapId;

	public SpawnMap() {
	}

	public SpawnMap(int mapId) {
		this.mapId = mapId;
		this.spawns = new ArrayList<>();
	}

	public int getMapId() {
		return mapId;
	}

	public List<Spawn> getSpawns() {
		return spawns == null ? Collections.emptyList() : spawns;
	}

	public List<BaseSpawn> getBaseSpawns() {
		return baseSpawns == null ? Collections.emptyList() : baseSpawns;
	}

	public List<MercenarySpawn> getMercenarySpawns() {
		return mercenarySpawns == null ? Collections.emptyList() : mercenarySpawns;
	}

	public List<RiftSpawn> getRiftSpawns() {
		return riftSpawns == null ? Collections.emptyList() : riftSpawns;
	}

	public List<SiegeSpawn> getSiegeSpawns() {
		return siegeSpawns == null ? Collections.emptyList() : siegeSpawns;
	}

	public List<VortexSpawn> getVortexSpawns() {
		return vortexSpawns == null ? Collections.emptyList() : vortexSpawns;
	}

	public List<AhserionsFlightSpawn> getAhserionSpawns() {
		return ahserionSpawns == null ? Collections.emptyList() : ahserionSpawns;
	}

}
