package com.aionemu.gameserver.dataholders;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.spawns.Spawn;
import com.aionemu.gameserver.model.templates.towns.TownLevel;
import com.aionemu.gameserver.model.templates.towns.TownSpawn;
import com.aionemu.gameserver.model.templates.towns.TownSpawnMap;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author ViAl
 */
@XmlRootElement(name = "town_spawns_data")
public class TownSpawnsData {

	@XmlElement(name = "spawn_map")
	private List<TownSpawnMap> spawnMap;

	private TIntObjectHashMap<TownSpawnMap> spawnMapsData = new TIntObjectHashMap<>();
	private Set<Integer> allNpcIds;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		allNpcIds = new HashSet<>();
		for (TownSpawnMap map : spawnMap) {
			Stream<Spawn> spawns = map.getTownSpawns().stream().flatMap(ts -> ts.getTownLevels().stream().flatMap(tl -> tl.getSpawns().stream()));
			allNpcIds.addAll(spawns.map(Spawn::getNpcId).collect(Collectors.toList()));
			spawnMapsData.put(map.getMapId(), map);
		}
		spawnMap.clear();
		spawnMap = null;
	}

	/**
	 * @return
	 */
	public int getSpawnsCount() {
		int counter = 0;
		for (TownSpawnMap spawnMap : spawnMapsData.valueCollection())
			for (TownSpawn townSpawn : spawnMap.getTownSpawns())
				for (TownLevel townLevel : townSpawn.getTownLevels())
					counter += townLevel.getSpawns().size();
		return counter;
	}

	/**
	 * @param townId
	 * @param townLevel
	 * @return
	 */
	public List<Spawn> getSpawns(int townId, int townLevel) {
		for (TownSpawnMap spawnMap : spawnMapsData.valueCollection()) {
			if (spawnMap.getTownSpawn(townId) != null) {
				TownSpawn townSpawn = spawnMap.getTownSpawn(townId);
				return townSpawn.getSpawnsForLevel(townLevel).getSpawns();
			}
		}
		return null;
	}

	public int getWorldIdForTown(int townId) {
		for (TownSpawnMap spawnMap : spawnMapsData.valueCollection())
			if (spawnMap.getTownSpawn(townId) != null)
				return spawnMap.getMapId();
		return 0;
	}

	/**
	 * @param npcId
	 * @return True, if the given npc appears in any of the spawn templates (town level 1-5)
	 */
	public boolean containsAnySpawnForNpc(int npcId) {
		return allNpcIds.contains(npcId);
	}
}
