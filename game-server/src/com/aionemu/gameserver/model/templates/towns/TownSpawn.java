package com.aionemu.gameserver.model.templates.towns;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ViAl
 */
@XmlType(name = "town_spawn")
public class TownSpawn {

	@XmlAttribute(name = "town_id")
	private int townId;
	@XmlElement(name = "town_level")
	private List<TownLevel> townLevels;

	@XmlTransient
	private final Map<Integer, TownLevel> townLevelsData = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		townLevelsData.clear();
		for (TownLevel level : townLevels) {
			townLevelsData.put(level.getLevel(), level);
		}
		townLevels = null;
	}

	public int getTownId() {
		return townId;
	}

	public TownLevel getSpawnsForLevel(int level) {
		return townLevelsData.get(level);
	}

	public Collection<TownLevel> getTownLevels() {
		return townLevelsData.values();
	}

}
