package com.aionemu.gameserver.dataholders;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.taskmanager.AbstractLockManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.event.EventTemplate;
import com.aionemu.gameserver.model.templates.spawns.*;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawn;
import com.aionemu.gameserver.model.templates.spawns.mercenaries.MercenaryRace;
import com.aionemu.gameserver.model.templates.spawns.mercenaries.MercenarySpawn;
import com.aionemu.gameserver.model.templates.spawns.mercenaries.MercenaryZone;
import com.aionemu.gameserver.model.templates.spawns.panesterra.AhserionsFlightSpawn;
import com.aionemu.gameserver.model.templates.spawns.riftspawns.RiftSpawn;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawn;
import com.aionemu.gameserver.model.templates.spawns.vortexspawns.VortexSpawn;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.xml.JAXBUtil;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.WorldType;

/**
 * @author xTz, Rolandas, Neon
 */
@XmlRootElement(name = "spawns")
@XmlType(namespace = "", name = "SpawnsData")
@XmlAccessorType(XmlAccessType.NONE)
public class SpawnsData extends AbstractLockManager {

	private static final Logger log = LoggerFactory.getLogger(SpawnsData.class);

	@XmlElement(name = "spawn_map", type = SpawnMap.class)
	private List<SpawnMap> templates;

	@XmlTransient
	private final Map<Integer, Map<Integer, List<SpawnGroup>>> allSpawnMaps = new HashMap<>();
	@XmlTransient
	private final Map<Integer, List<SpawnGroup>> baseSpawnMaps = new HashMap<>();
	@XmlTransient
	private final Map<Integer, List<SpawnGroup>> riftSpawnMaps = new HashMap<>();
	@XmlTransient
	private final Map<Integer, List<SpawnGroup>> siegeSpawnMaps = new HashMap<>();
	@XmlTransient
	private final Map<Integer, List<SpawnGroup>> vortexSpawnMaps = new HashMap<>();
	@XmlTransient
	private final Map<Integer, MercenarySpawn> mercenarySpawns = new HashMap<>();
	@XmlTransient
	private final Map<Integer, List<SpawnGroup>> ahserionSpawnMaps = new HashMap<>(); // Ahserion's flight
	@XmlTransient
	private Set<Integer> allNpcIds;

	public void afterUnmarshal(Unmarshaller u, Object parent) {
		for (SpawnMap map : templates) {
			Map<Integer, List<SpawnGroup>> mapSpawns = allSpawnMaps.get(map.getMapId());
			writeLock();
			try {
				if (mapSpawns == null) {
					mapSpawns = new HashMap<>();
						allSpawnMaps.put(map.getMapId(), mapSpawns);
				}

				List<Integer> customs = new ArrayList<>();
				for (Spawn spawn : map.getSpawns()) {
					if (customs.contains(spawn.getNpcId()))
						continue;
					if (spawn.isCustom() && spawn.getEventTemplate() == null) { // custom event spawns are handled in Event class
						mapSpawns.remove(spawn.getNpcId());
						customs.add(spawn.getNpcId());
					}
					List<SpawnGroup> spawnGroups = mapSpawns.get(spawn.getNpcId());
					if (spawnGroups == null) {
						spawnGroups = new ArrayList<>(1);
						mapSpawns.put(spawn.getNpcId(), spawnGroups);
					}
					spawnGroups.add(new SpawnGroup(map.getMapId(), spawn));
				}
			} finally {
				writeUnlock();
			}

			for (BaseSpawn baseSpawn : map.getBaseSpawns()) {
				int baseId = baseSpawn.getId();
				if (!baseSpawnMaps.containsKey(baseId)) {
					baseSpawnMaps.put(baseId, new ArrayList<>());
				}
				for (BaseSpawn.SimpleRaceTemplate simpleRace : baseSpawn.getBaseRaceTemplates()) {
					for (Spawn spawn : simpleRace.getSpawns()) {
						SpawnGroup spawnGroup = new SpawnGroup(map.getMapId(), spawn, baseId, simpleRace.getBaseRace());
						baseSpawnMaps.get(baseId).add(spawnGroup);
					}
				}
			}

			for (RiftSpawn rift : map.getRiftSpawns()) {
				int id = rift.getId();
				if (!riftSpawnMaps.containsKey(id)) {
					riftSpawnMaps.put(id, new ArrayList<>());
				}
				for (Spawn spawn : rift.getSpawns()) {
					SpawnGroup spawnGroup = new SpawnGroup(map.getMapId(), spawn, id);
					riftSpawnMaps.get(id).add(spawnGroup);
				}
			}

			for (SiegeSpawn SiegeSpawn : map.getSiegeSpawns()) {
				int siegeId = SiegeSpawn.getSiegeId();
				if (!siegeSpawnMaps.containsKey(siegeId)) {
					siegeSpawnMaps.put(siegeId, new ArrayList<>());
				}
				for (SiegeSpawn.SiegeRaceTemplate race : SiegeSpawn.getSiegeRaceTemplates()) {
					for (SiegeSpawn.SiegeRaceTemplate.SiegeModTemplate mod : race.getSiegeModTemplates()) {
						if (mod == null || mod.getSpawns() == null) {
							continue;
						}
						for (Spawn spawn : mod.getSpawns()) {
							SpawnGroup spawnGroup = new SpawnGroup(map.getMapId(), spawn, siegeId, race.getSiegeRace(), mod.getSiegeModType());
							siegeSpawnMaps.get(siegeId).add(spawnGroup);
						}
					}
				}
			}

			for (VortexSpawn VortexSpawn : map.getVortexSpawns()) {
				int id = VortexSpawn.getId();
				if (!vortexSpawnMaps.containsKey(id)) {
					vortexSpawnMaps.put(id, new ArrayList<>());
				}
				for (VortexSpawn.VortexStateTemplate type : VortexSpawn.getSiegeModTemplates()) {
					if (type == null || type.getSpawns() == null) {
						continue;
					}
					for (Spawn spawn : type.getSpawns()) {
						SpawnGroup spawnGroup = new SpawnGroup(map.getMapId(), spawn, id, type.getStateType());
						vortexSpawnMaps.get(id).add(spawnGroup);
					}
				}
			}

			for (MercenarySpawn mercenarySpawn : map.getMercenarySpawns()) {
				int id = mercenarySpawn.getSiegeId();
				mercenarySpawns.put(id, mercenarySpawn);
				for (MercenaryRace mrace : mercenarySpawn.getMercenaryRaces()) {
					for (MercenaryZone mzone : mrace.getMercenaryZones()) {
						mzone.setWorldId(map.getMapId());
						mzone.setSiegeId(mercenarySpawn.getSiegeId());
					}

				}
			}

			for (AhserionsFlightSpawn ahserionSpawn : map.getAhserionSpawns()) {
				int teamId = ahserionSpawn.getFaction().getId();
				if (!ahserionSpawnMaps.containsKey(teamId)) {
					ahserionSpawnMaps.put(teamId, new ArrayList<>());
				}

				for (AhserionsFlightSpawn.AhserionStageSpawnTemplate stageTemplate : ahserionSpawn.getStageSpawnTemplate()) {
					if (stageTemplate == null || stageTemplate.getSpawns() == null)
						continue;

					for (Spawn spawn : stageTemplate.getSpawns()) {
						SpawnGroup spawnGroup = new SpawnGroup(map.getMapId(), spawn, stageTemplate.getStage(), ahserionSpawn.getFaction());
						ahserionSpawnMaps.get(teamId).add(spawnGroup);
					}
				}
			}
		}
		allNpcIds = allSpawnMaps.values().stream().flatMap(spawn -> spawn.keySet().stream()).collect(Collectors.toSet());
		allNpcIds.addAll(baseSpawnMaps.values().stream().flatMap(group -> group.stream().map(SpawnGroup::getNpcId)).collect(Collectors.toSet()));
		allNpcIds
			.addAll(siegeSpawnMaps.values().stream().flatMap(group -> group.stream().map(SpawnGroup::getNpcId)).collect(Collectors.toSet()));
		allNpcIds.addAll(riftSpawnMaps.values().stream().flatMap(group -> group.stream().map(SpawnGroup::getNpcId)).collect(Collectors.toSet()));
		allNpcIds
			.addAll(vortexSpawnMaps.values().stream().flatMap(group -> group.stream().map(SpawnGroup::getNpcId)).collect(Collectors.toSet()));
		allNpcIds
			.addAll(ahserionSpawnMaps.values().stream().flatMap(group -> group.stream().map(SpawnGroup::getNpcId)).collect(Collectors.toSet()));
	}

	public void clearTemplates() {
		templates = null;
	}

	public List<SpawnGroup> getSpawnsByWorldId(int worldId) {
		readLock();
		try {
			Map<Integer, List<SpawnGroup>> spawnGroupsByNpcId = allSpawnMaps.get(worldId);
			if (spawnGroupsByNpcId == null)
				return Collections.emptyList();
			return spawnGroupsByNpcId.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
		} finally {
			readUnlock();
		}
	}

	public List<SpawnGroup> getSpawnsForNpc(int worldId, int npcId) {
		readLock();
		try {
			Map<Integer, List<SpawnGroup>> spawnGroupsByNpcId = allSpawnMaps.get(worldId);
			List<SpawnGroup> spawnGroups = spawnGroupsByNpcId == null ? null : spawnGroupsByNpcId.get(npcId);
			return spawnGroups == null ? Collections.emptyList() : spawnGroups;
		} finally {
			readUnlock();
		}
	}

	public List<SpawnGroup> getBaseSpawnsByLocId(int id) {
		return baseSpawnMaps.get(id);
	}

	public List<SpawnGroup> getRiftSpawnsByLocId(int id) {
		return riftSpawnMaps.get(id);
	}

	public List<SpawnGroup> getSiegeSpawnsByLocId(int siegeId) {
		return siegeSpawnMaps.get(siegeId);
	}

	public List<SpawnGroup> getVortexSpawnsByLocId(int id) {
		return vortexSpawnMaps.get(id);
	}

	public MercenarySpawn getMercenarySpawnBySiegeId(int id) {
		return mercenarySpawns.get(id);
	}

	public synchronized boolean saveSpawn(VisibleObject visibleObject, boolean delete) {
		SpawnTemplate spawn = visibleObject.getSpawn();
		if (spawn == null) // some objects like house objects have no spawn template
			return false;
		if (!spawn.getClass().equals(SpawnTemplate.class)) // do not save special/temporary spawns (siege, base, rift spawn, ...) as world spawns
			return false;
		if (spawn.getRespawnTime() <= 0) // do not save single time spawns (world raid, handler spawn, ...) as world spawns
			return false;
		if (spawn.isTemporarySpawn()) // spawn start and end times of temporary world spawns (shugos, agrints, ...) would get lost
			return false;

		String folder = "./data/static_data/spawns/" + getRelativePath(visibleObject);
		String fileName = visibleObject.getWorldId() + "_" + visibleObject.getPosition().getWorldMapInstance().getParent().getName().replace(' ', '_')
			+ ".xml";
		File xml = new File(folder + "/New/" + fileName);
		String schema = "./data/static_data/spawns/spawns.xsd";
		SpawnsData data = xml.isFile() ? JAXBUtil.deserialize(xml, SpawnsData.class, schema) : new SpawnsData();
		SpawnMap spawnMap = data.templates == null ? null
			: data.templates.stream().filter(m -> m.getMapId() == visibleObject.getWorldId()).findFirst().orElse(null);
		if (spawnMap == null) {
			spawnMap = new SpawnMap(visibleObject.getWorldId());
			if (data.templates == null)
				data.templates = Collections.singletonList(spawnMap);
			else
				data.templates.add(spawnMap);
		}
		Spawn oldGroup = findSpawnTemplate(spawnMap, spawn, delete); // find in new file
		if (oldGroup == null) {
			oldGroup = loadSpawnsFromTemplateFiles(folder, schema, spawn, delete); // load from old files
			if (oldGroup != null)
				spawnMap.getSpawns().add(oldGroup);
		}
		if (oldGroup == null) {
			oldGroup = new Spawn(spawn.getNpcId(), spawn.getRespawnTime(), spawn.getHandlerType());
			spawnMap.getSpawns().add(oldGroup);
		}
		oldGroup.setCustom(true);

		SpawnSpotTemplate spot = new SpawnSpotTemplate(visibleObject.getX(), visibleObject.getY(), visibleObject.getZ(), visibleObject.getHeading(),
			visibleObject.getSpawn().getRandomWalkRange(), visibleObject.getSpawn().getWalkerId(), visibleObject.getSpawn().getWalkerIndex());
		int oldSpotIndex = -1;
		for (int i = 0; i < oldGroup.getSpawnSpotTemplates().size(); i++) {
			SpawnSpotTemplate s = oldGroup.getSpawnSpotTemplates().get(i);
			if (positionMatches(spawn, s)) {
				oldSpotIndex = i;
				break;
			}
		}
		if (oldSpotIndex >= 0) {
			if (delete)
				oldGroup.getSpawnSpotTemplates().remove(oldSpotIndex);
			else
				oldGroup.getSpawnSpotTemplates().set(oldSpotIndex, spot);
		} else if (!delete)
			oldGroup.getSpawnSpotTemplates().add(spot);


		xml.getParentFile().mkdir();
		try {
			Files.writeString(xml.toPath(), JAXBUtil.serialize(data, schema));
		} catch (Exception e) {
			log.error("Could not save XML file!", e);
			return false;
		}
		// update spawn coords at the end, because we need previous coords above to find the old spawn template
		spawn.setX(spot.getX());
		spawn.setY(spot.getY());
		spawn.setZ(spot.getZ());
		spawn.setHeading(spot.getHeading());
		templates = data.templates;
		afterUnmarshal(null, null);
		clearTemplates();
		return true;
	}

	private Spawn loadSpawnsFromTemplateFiles(String folder, String schema, SpawnTemplate spawn, boolean exactMatch) {
		AtomicReference<Spawn> match = new AtomicReference<>();
		try {
			Files.walkFileTree(Paths.get(folder), new SimpleFileVisitor<>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					if (attrs.isRegularFile() && file.toString().toLowerCase().endsWith(".xml")) {
						for (SpawnMap spawnMap : JAXBUtil.deserialize(file.toFile(), SpawnsData.class, schema).templates) {
							Spawn s = findSpawnTemplate(spawnMap, spawn, exactMatch);
							if (s != null) {
								match.set(s);
								return FileVisitResult.TERMINATE;
							}
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			log.error("", e);
		}
		return match.get();
	}

	private Spawn findSpawnTemplate(SpawnMap spawnMap, SpawnTemplate spawn, boolean exactMatch) {
		if (spawnMap.getMapId() != spawn.getWorldId())
			return null;
		return spawnMap.getSpawns().stream()
			.filter(
				s -> s.getNpcId() == spawn.getNpcId() && (!exactMatch || s.getSpawnSpotTemplates().stream().anyMatch(spot -> positionMatches(spawn, spot))))
			.findFirst().orElse(null);
	}

	private boolean positionMatches(SpawnTemplate spawn, SpawnSpotTemplate spawnSpotTemplate) {
		return spawnSpotTemplate.getX() == spawn.getX() && spawnSpotTemplate.getY() == spawn.getY() && spawnSpotTemplate.getZ() == spawn.getZ()
			&& spawnSpotTemplate.getHeading() == spawn.getHeading();
	}

	private static String getRelativePath(VisibleObject visibleObject) {
		if (visibleObject.getSpawn().getHandlerType() == SpawnHandlerType.RIFT)
			return "Rifts";
		else if (visibleObject instanceof Gatherable)
			return "Gather";
		else if (visibleObject.getPosition().getWorldMapInstance().getParent().isInstanceType())
			return "Instances";
		else
			return "Npcs";
	}

	public int size() {
		return allSpawnMaps.size();
	}

	/**
	 * first search: current map
	 * second search: all maps of players race
	 * third search: all other maps
	 * 
	 * @param player
	 * @param npcId
	 * @param worldId
	 * @return
	 */
	public SpawnSearchResult getNearestSpawnByNpcId(Player player, int npcId, int worldId) {
		List<SpawnGroup> spawns = getSpawnsForNpc(worldId, npcId);
		if (spawns == null) { // -> there are no spawns for this npcId on the current map
			// search all maps of players race
			for (WorldMapTemplate template : DataManager.WORLD_MAPS_DATA) {
				if (template.getMapId() == worldId)
					continue;
				if ((template.getWorldType() == WorldType.ELYSEA && player.getRace() == Race.ELYOS)
					|| (template.getWorldType() == WorldType.ASMODAE && player.getRace() == Race.ASMODIANS)) {
					spawns = getSpawnsForNpc(template.getMapId(), npcId);
					if (spawns != null) {
						worldId = template.getMapId();
						break;
					}
				}
			}

			// -> there are no spawns for this npcId on all maps of players race
			// search all other maps
			if (spawns == null) {
				for (WorldMapTemplate template : DataManager.WORLD_MAPS_DATA) {
					if ((template.getMapId() == worldId) || (template.getWorldType() == WorldType.ELYSEA && player.getRace() == Race.ELYOS)
						|| (template.getWorldType() == WorldType.ASMODAE && player.getRace() == Race.ASMODIANS)) {
						continue;
					}
					spawns = getSpawnsForNpc(template.getMapId(), npcId);
					if (spawns != null) {
						worldId = template.getMapId();
						break;
					}
				}
			}

			if (spawns == null) {
				return null;
			}
		}

		return getNearestSpawn((player != null ? player.getPosition() : null), spawns, worldId);
	}

	private SpawnSearchResult getNearestSpawn(WorldPosition position, List<SpawnGroup> spawnGroups, int worldId) {
		if (position == null || spawnGroups.isEmpty()) {
			return null;
		}
		if (worldId != position.getMapId()) {
			SpawnGroup spawnGroup = spawnGroups.get(0);
			return spawnGroup.getSpawnTemplates().isEmpty() ? null : toSpawnSearchResult(worldId, spawnGroup.getSpawnTemplates().get(0));
		}

		SpawnTemplate temp = null;
		float distance = 0;
		outerLoop:
		for (SpawnGroup spawnGroup : spawnGroups) {
			for (SpawnTemplate spot : spawnGroup.getSpawnTemplates()) {
				if (temp == null) {
					temp = spot;
					distance = (float) PositionUtil.getDistance(position.getX(), position.getY(), position.getZ(), spot.getX(), spot.getY(), spot.getZ());
					if (distance <= 1f)
						break outerLoop;
				} else {
					float dist = (float) PositionUtil.getDistance(position.getX(), position.getY(), position.getZ(), spot.getX(), spot.getY(), spot.getZ());
					if (dist < distance) {
						distance = dist;
						temp = spot;
						if (distance <= 1f)
							break outerLoop;
					}
				}
			}
		}

		return temp == null ? null : toSpawnSearchResult(worldId, temp);
	}

	private SpawnSearchResult toSpawnSearchResult(int worldId, SpawnTemplate spot) {
		return new SpawnSearchResult(worldId, new SpawnSpotTemplate(spot.getX(), spot.getY(), spot.getZ(), spot.getHeading(), spot.getRandomWalkRange(),
			spot.getWalkerId(), spot.getWalkerIndex()));
	}

	/**
	 * @param worldId
	 *          Optional. If provided, searches in this world first
	 * @param npcId
	 * @return template for the spot
	 */
	public SpawnSearchResult getFirstSpawnByNpcId(int worldId, int npcId) {
		List<SpawnGroup> spawnGroups = getSpawnsForNpc(worldId, npcId);

		if (spawnGroups.isEmpty()) {
			for (WorldMapTemplate template : DataManager.WORLD_MAPS_DATA) {
				if (template.getMapId() == worldId)
					continue;
				spawnGroups = getSpawnsForNpc(template.getMapId(), npcId);
				if (!spawnGroups.isEmpty()) {
					worldId = template.getMapId();
					break;
				}
			}
			if (spawnGroups.isEmpty())
				return null;
		}
		List<SpawnTemplate> spawnSpots = spawnGroups.get(0).getSpawnTemplates();
		return spawnSpots.isEmpty() ? null : toSpawnSearchResult(worldId, spawnSpots.get(0));
	}

	/**
	 * Used by Event Service to add additional spawns
	 * 
	 * @param spawnMap
	 *          templates to add
	 */
	public void addNewSpawnMap(SpawnMap spawnMap) {
		if (templates == null)
			templates = new ArrayList<>();
		templates.add(spawnMap);
	}

	public void removeEventSpawnObjects(EventTemplate eventTemplate) {
		writeLock();
		try {
			allSpawnMaps.values().forEach(spawnGroupsByNpcId -> {
				Collection<List<SpawnGroup>> allSpawnGroups = spawnGroupsByNpcId.values();
				allSpawnGroups.forEach(spawnGroups -> spawnGroups.removeIf(spawnGroup -> eventTemplate.equals(spawnGroup.getEventTemplate())));
				allSpawnGroups.removeIf(List::isEmpty);
			});
		} finally {
			writeUnlock();
		}
	}

	public List<SpawnMap> getTemplates() {
		return templates;
	}

	public List<SpawnGroup> getAhserionSpawnByTeamId(int id) {
		return ahserionSpawnMaps.get(id);
	}

	/**
	 * @return All npc ids which appear in the spawn templates.
	 */
	public Set<Integer> getAllNpcIds() {
		return allNpcIds;
	}

	/**
	 * @param npcId
	 * @return True, if the given npc appears in any of the spawn templates (world, instance, siege, base, ...)
	 */
	public boolean containsAnySpawnForNpc(int npcId) {
		return allNpcIds.contains(npcId);
	}
}
