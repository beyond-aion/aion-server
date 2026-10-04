package com.aionemu.gameserver.model.templates.spawns.mercenaries;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author ViAl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MercenarySpawn")
public class MercenarySpawn {

	@XmlAttribute(name = "siege_id")
	private int siegeId;
	@XmlElement(name = "mercenary_race")
	private List<MercenaryRace> mercenaryRaces;

	public int getSiegeId() {
		return siegeId;
	}

	public List<MercenaryRace> getMercenaryRaces() {
		return mercenaryRaces;
	}

}
