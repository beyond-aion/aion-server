package com.aionemu.gameserver.model.templates.spawns.assaults;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.siege.AssaultType;
import com.aionemu.gameserver.model.templates.spawns.Spawn;

/**
 * @author Whoop
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssaultWave")
public class AssaultWave {

	@XmlAttribute(name = "wave")
	private AssaultType wave;
	@XmlElement(name = "spawn")
	private List<Spawn> spawns;
	@XmlTransient
	private int worldId;
	@XmlTransient
	private int siegeId;

	public AssaultType getAssaultType() {
		return wave;
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
