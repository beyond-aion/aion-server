package com.aionemu.gameserver.model.templates.globaldrops;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
