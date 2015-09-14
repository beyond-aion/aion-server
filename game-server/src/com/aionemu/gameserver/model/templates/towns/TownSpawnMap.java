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
@XmlType(name = "town_spawn_map")
public class TownSpawnMap {

	@XmlAttribute(name = "map_id")
	private int mapId;
	@XmlElement(name = "town_spawn")
	private List<TownSpawn> townSpawns;

	private TIntObjectHashMap<TownSpawn> townSpawnsData = new TIntObjectHashMap<TownSpawn>();

	/**
	 * @param u
	 * @param parent
	 */
	void afterUnmarshal(Unmarshaller u, Object parent) {
		townSpawnsData.clear();

		for (TownSpawn town : townSpawns) {
			townSpawnsData.put(town.getTownId(), town);
		}
		townSpawns.clear();
		townSpawns = null;
	}

	/**
	 * @return the mapId
	 */
	public int getMapId() {
		return mapId;
	}

	public TownSpawn getTownSpawn(int townId) {
		return townSpawnsData.get(townId);
	}

	public Collection<TownSpawn> getTownSpawns() {
		return townSpawnsData.valueCollection();
	}

}
