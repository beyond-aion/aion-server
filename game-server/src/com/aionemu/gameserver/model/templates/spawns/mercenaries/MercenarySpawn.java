package com.aionemu.gameserver.model.templates.spawns.mercenaries;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
