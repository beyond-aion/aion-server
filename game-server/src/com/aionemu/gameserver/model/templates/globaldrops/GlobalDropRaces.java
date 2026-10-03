package com.aionemu.gameserver.model.templates.globaldrops;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author AionCool
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropRaces")
public class GlobalDropRaces {

	@XmlElement(name = "gd_race")
	protected List<GlobalDropRace> gdRaces;

	public List<GlobalDropRace> getGlobalDropRaces() {
		if (gdRaces == null) {
			gdRaces = new ArrayList<>();
		}
		return this.gdRaces;
	}

}
