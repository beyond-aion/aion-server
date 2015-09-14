package com.aionemu.gameserver.model.templates.spawns;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.spawns.assaults.AssaultSpawn;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawn;
import com.aionemu.gameserver.model.templates.spawns.mercenaries.MercenarySpawn;
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
	@XmlElement(name = "assault_spawn")
	private List<AssaultSpawn> assaultSpawns;
	@XmlElement(name = "vortex_spawn")
	private List<VortexSpawn> vortexSpawns;
	@XmlAttribute(name = "map_id")
	private int mapId;

	public SpawnMap() {
	}

	public SpawnMap(int mapId) {
		this.mapId = mapId;
	}

	public int getMapId() {
		return mapId;
	}

	public List<Spawn> getSpawns() {
		if (spawns == null) {
			spawns = new ArrayList<Spawn>();
		}
		return spawns;
	}

	public void addSpawns(Spawn spawns) {
		getSpawns().add(spawns);
	}

	public void removeSpawns(Spawn spawns) {
		getSpawns().remove(spawns);
	}

	public List<BaseSpawn> getBaseSpawns() {
		if (baseSpawns == null) {
			baseSpawns = new ArrayList<BaseSpawn>();
		}
		return baseSpawns;
	}

	public List<MercenarySpawn> getMercenarySpawns() {
		if (mercenarySpawns == null)
			mercenarySpawns = new ArrayList<MercenarySpawn>(0);
		return mercenarySpawns;
	}

	public List<AssaultSpawn> getAssaultSpawns() {
		if (assaultSpawns == null)
			assaultSpawns = new ArrayList<AssaultSpawn>(0);
		return assaultSpawns;
	}

	public void addBaseSpawns(BaseSpawn spawns) {
		getBaseSpawns().add(spawns);
	}

	public List<RiftSpawn> getRiftSpawns() {
		if (riftSpawns == null) {
			riftSpawns = new ArrayList<RiftSpawn>();
		}
		return riftSpawns;
	}

	public void addRiftSpawns(RiftSpawn spawns) {
		getRiftSpawns().add(spawns);
	}

	public List<SiegeSpawn> getSiegeSpawns() {
		if (siegeSpawns == null) {
			siegeSpawns = new ArrayList<SiegeSpawn>();
		}
		return siegeSpawns;
	}

	public void addSiegeSpawns(SiegeSpawn spawns) {
		getSiegeSpawns().add(spawns);
	}

	public List<VortexSpawn> getVortexSpawns() {
		if (vortexSpawns == null) {
			vortexSpawns = new ArrayList<VortexSpawn>();
		}
		return vortexSpawns;
	}

	public void addVortexSpawns(VortexSpawn spawns) {
		getVortexSpawns().add(spawns);
	}

}
