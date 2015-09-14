package com.aionemu.gameserver.model.templates.globaldrops;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;

/**
 * @author AionCool
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropRace")
public class GlobalDropRace {

	@XmlAttribute(name = "race", required = true)
	protected Race race;

	public Race getRace() {
		return race;
	}
}
