package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.templates.spawns.Spawn;
import com.aionemu.gameserver.model.templates.towns.TownLevel;
import com.aionemu.gameserver.model.templates.towns.TownSpawn;
import com.aionemu.gameserver.model.templates.towns.TownSpawnMap;

/**
 * @author ViAl
 */
@XmlRootElement(name = "town_spawns_data")
public class TownSpawnsData {

	@XmlElement(name = "spawn_map")
	private List<TownSpawnMap> spawnMap;

	@XmlTransient
	private final Map<Integer, TownSpawnMap> spawnMapsData = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (TownSpawnMap map : spawnMap)
			spawnMapsData.put(map.getMapId(), map);
		spawnMap = null;
	}

	public int getSpawnsCount() {
		int counter = 0;
		for (TownSpawnMap spawnMap : spawnMapsData.values())
			for (TownSpawn townSpawn : spawnMap.getTownSpawns())
				for (TownLevel townLevel : townSpawn.getTownLevels())
					counter += townLevel.getSpawns().size();
		return counter;
	}

	public List<Spawn> getSpawns(int townId, int townLevel) {
		for (TownSpawnMap spawnMap : spawnMapsData.values()) {
			if (spawnMap.getTownSpawn(townId) != null) {
				TownSpawn townSpawn = spawnMap.getTownSpawn(townId);
				return townSpawn.getSpawnsForLevel(townLevel).getSpawns();
			}
		}
		return null;
	}

	public int getWorldIdForTown(int townId) {
		for (TownSpawnMap spawnMap : spawnMapsData.values())
			if (spawnMap.getTownSpawn(townId) != null)
				return spawnMap.getMapId();
		return 0;
	}

	public void addAllNpcIdsToSet(Set<Integer> npcIds) {
		spawnMapsData.values().stream()
			.flatMap(map-> map.getTownSpawns().stream())
			.flatMap(ts -> ts.getTownLevels().stream().flatMap(tl -> tl.getSpawns().stream()))
			.map(Spawn::getNpcId)
			.forEach(npcIds::add);
	}
}
