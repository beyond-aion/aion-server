package com.aionemu.gameserver.spawnengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Rolandas
 */
public class InstanceWalkerFormations {

	private static final Logger log = LoggerFactory.getLogger(InstanceWalkerFormations.class);

	private Map<String, List<ClusteredNpc>> groupedSpawnObjects;
	private Map<String, WalkerGroup> walkFormations;
	private Map<String, List<WalkerGroup>> formationVariants;
	private Map<String, List<ClusteredNpc>> walkerVariants;

	public InstanceWalkerFormations() {
		groupedSpawnObjects = new HashMap<>();
		walkFormations = new HashMap<>();
		formationVariants = new HashMap<>();
		walkerVariants = new HashMap<>();
	}

	public WalkerGroup getSpawnWalkerGroup(String walkerId) {
		return walkFormations.get(walkerId);
	}

	protected synchronized boolean cacheWalkerCandidate(ClusteredNpc npcWalker) {
		String walkerId = npcWalker.getWalkTemplate().getRouteId();
		List<ClusteredNpc> candidateList = groupedSpawnObjects.get(walkerId);
		if (candidateList == null) {
			candidateList = new ArrayList<>();
			groupedSpawnObjects.put(walkerId, candidateList);
		}
		return candidateList.add(npcWalker);
	}

	/**
	 * Organizes spawns in all processed walker groups. Must be called only when spawning all npcs for the instance of world.
	 */
	protected void organizeAndSpawn() {
		for (List<ClusteredNpc> candidates : groupedSpawnObjects.values()) {
			Map<Integer, List<ClusteredNpc>> npcsByPosition = candidates.stream().collect(Collectors.groupingBy(cNpc -> cNpc.getPositionHash()));
			int maxSize = 0;
			List<ClusteredNpc> npcs = null;
			for (Entry<Integer, List<ClusteredNpc>> e : npcsByPosition.entrySet()) {
				if (e.getValue().size() > maxSize) {
					npcs = e.getValue();
					maxSize = npcs.size();
				}
			}
			if (maxSize == 0 || npcs == null) {
				log.warn("Walkers missing for route: " + candidates.get(0).getWalkTemplate().getRouteId());
				continue;
			}
			if (maxSize == 1) {
				if (candidates.size() != 1) {
					log.warn("Walkers not aligned for route: " + candidates.get(0).getWalkTemplate().getRouteId());
					for (ClusteredNpc snpc : candidates)
						snpc.spawn(snpc.getNpc().getSpawn().getZ());
				} else {
					ClusteredNpc singleNpc = candidates.get(0);
					if (singleNpc.getWalkTemplate().getVersionId() != null) {
						List<ClusteredNpc> variants = walkerVariants.get(singleNpc.getWalkTemplate().getVersionId());
						if (variants == null) {
							variants = new ArrayList<>();
							walkerVariants.put(singleNpc.getWalkTemplate().getVersionId(), variants);
						}
						variants.add(singleNpc);
					} else
						singleNpc.spawn(singleNpc.getNpc().getSpawn().getZ());
				}
			} else {
				WalkerGroup wg = new WalkerGroup(npcs);
				if (candidates.get(0).getWalkTemplate().getPool() != candidates.size())
					log.warn("Incorrect pool for route: " + candidates.get(0).getWalkTemplate().getRouteId());
				walkFormations.put(candidates.get(0).getWalkTemplate().getRouteId(), wg);
				wg.form();
				if (wg.getVersionId() == null) {
					wg.spawn();
					// spawn the rest which didn't have the same coordinates
					for (ClusteredNpc snpc : candidates) {
						if (npcs.contains(snpc))
							continue;
						snpc.spawn(snpc.getNpc().getZ());
					}
				} else {
					List<WalkerGroup> variants = formationVariants.get(wg.getVersionId());
					if (variants == null) {
						variants = new ArrayList<>();
						formationVariants.put(wg.getVersionId(), variants);
					}
					variants.add(wg);
				}
			}
			// Now that all variants are in the map, spawn one randomly
			for (List<WalkerGroup> varGroups : formationVariants.values()) {
				WalkerGroup spawnedGroup = Rnd.get(varGroups);
				spawnedGroup.spawn();
			}
			for (List<ClusteredNpc> varWalkers : walkerVariants.values()) {
				ClusteredNpc spawnedWalker = Rnd.get(varWalkers);
				spawnedWalker.spawn(spawnedWalker.getNpc().getZ());
			}
		}
	}

	protected void changeCluster(WalkerGroup walkerGroup) {
		if (walkerGroup.getVersionId() == null)
			return;
		List<WalkerGroup> varGroups = formationVariants.get(walkerGroup.getVersionId());
		if (varGroups == null)
			return;
		List<WalkerGroup> notSpawned = varGroups.stream().filter(group -> !group.isSpawned()).collect(Collectors.toList());
		WalkerGroup newGroup = Rnd.get(notSpawned);
		newGroup.spawn();
		if (walkerGroup.isSpawned())
			walkerGroup.despawn();
	}

	protected void changeWalker(Npc npc) {
		String walkerId = npc.getSpawn().getWalkerId();
		if (walkerId == null)
			return;
		String versionId = DataManager.WALKER_VERSIONS_DATA.getRouteVersionId(walkerId);
		if (versionId == null)
			return;
		List<ClusteredNpc> varWalkers = walkerVariants.get(versionId);
		if (varWalkers == null)
			return;
		List<ClusteredNpc> notSpawned = varWalkers.stream().filter(cNpc -> !cNpc.getNpc().isSpawned()).collect(Collectors.toList());
		ClusteredNpc newWalker = Rnd.get(notSpawned);
		newWalker.spawn(newWalker.getNpc().getZ());
		if (!npc.isSpawned())
			return;
		for (ClusteredNpc snpc : varWalkers) {
			if (snpc.getNpc().equals(npc)) {
				snpc.despawn();
				break;
			}
		}
	}

	protected synchronized void onInstanceDestroy() {
		groupedSpawnObjects.clear();
		walkFormations.clear();
	}

}
