package com.aionemu.gameserver.model.templates.spawns.mercenaries;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.spawns.Spawn;

/**
 * @author ViAl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MercenaryZone")
public class MercenaryZone {

	@XmlAttribute(name = "zone")
	private int zone;
	@XmlElement(name = "spawn")
	private List<Spawn> spawns;
	@XmlTransient
	private int worldId;
	@XmlTransient
	private int siegeId;

	public int getZoneId() {
		return zone;
	}

	public List<Spawn> getSpawns() {
		return spawns;
	}

	public int getWorldId() {
		return worldId;
	}

	public void setWorldId(int worldId) {
		this.worldId = worldId;
	}

	public int getSiegeId() {
		return siegeId;
	}

	public void setSiegeId(int siegeId) {
		this.siegeId = siegeId;
	}

}
