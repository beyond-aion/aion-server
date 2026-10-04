package com.aionemu.gameserver.model.templates.towns;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author ViAl
 */
@XmlType(name = "town_spawn_map")
public class TownSpawnMap {

	@XmlAttribute(name = "map_id")
	private int mapId;
	@XmlElement(name = "town_spawn")
	private List<TownSpawn> townSpawns;

	@XmlTransient
	private final Map<Integer, TownSpawn> townSpawnsData = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		townSpawnsData.clear();
		for (TownSpawn town : townSpawns) {
			townSpawnsData.put(town.getTownId(), town);
		}
		townSpawns = null;
	}

	public int getMapId() {
		return mapId;
	}

	public TownSpawn getTownSpawn(int townId) {
		return townSpawnsData.get(townId);
	}

	public Collection<TownSpawn> getTownSpawns() {
		return townSpawnsData.values();
	}

}
