package com.aionemu.gameserver.model.templates.spawns.assaults;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Whoop
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssaultSpawn")
public class AssaultSpawn {

	@XmlAttribute(name = "siege_id")
	private int siegeId;
	@XmlElement(name = "assault_wave")
	private List<AssaultWave> assaultWaves;

	public int getSiegeId() {
		return siegeId;
	}

	public List<AssaultWave> getAssaultWaves() {
		return assaultWaves;
	}

}
