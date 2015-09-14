package com.aionemu.gameserver.model.templates.spawns.mercenaries;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;

/**
 * @author ViAl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MercenaryRace")
public class MercenaryRace {

	@XmlAttribute(name = "race")
	private Race race;
	@XmlElement(name = "mercenary_zone")
	private List<MercenaryZone> mercenaryZones;

	public Race getRace() {
		return race;
	}

	public List<MercenaryZone> getMercenaryZones() {
		return mercenaryZones;
	}

}
