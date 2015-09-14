package com.aionemu.gameserver.model.templates.towns;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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

	private TIntObjectHashMap<TownLevel> townLevelsData = new TIntObjectHashMap<TownLevel>();

	/**
	 * @param u
	 * @param parent
	 */
	void afterUnmarshal(Unmarshaller u, Object parent) {
		townLevelsData.clear();

		for (TownLevel level : townLevels) {
			townLevelsData.put(level.getLevel(), level);
		}
		townLevels.clear();
		townLevels = null;
	}

	/**
	 * @return the townId
	 */
	public int getTownId() {
		return townId;
	}

	public TownLevel getSpawnsForLevel(int level) {
		return townLevelsData.get(level);
	}

	public Collection<TownLevel> getTownLevels() {
		return this.townLevelsData.valueCollection();
	}

}
