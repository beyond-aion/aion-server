package com.aionemu.gameserver.world;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.templates.zone.ZoneClassName;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * Just some part of map.
 * 
 * @author -Nemesiss-
 */
public class MapRegion {

	private static final Logger log = LoggerFactory.getLogger(MapRegion.class);

	/**
	 * Region ID of this map region (not world ID).
	 */
	private final int regionId;
	/**
	 * WorldMapInstance which is parent of this map region.
	 */
	private final WorldMapInstance parent;
	/**
	 * Surrounding regions + self.
	 */
	private MapRegion[] neighbours = new MapRegion[] { this };
	/**
	 * Objects in this map region.
	 */
	private final Map<Integer, VisibleObject> objects = new ConcurrentHashMap<>();

	private final AtomicInteger playerCount = new AtomicInteger(0);

	private final AtomicBoolean regionActive = new AtomicBoolean(false);

	private final int zoneCount;

	/**
	 * Zones in this region
	 */
	private Map<Integer, TreeSet<ZoneInstance>> zoneMap;

	MapRegion(int id, WorldMapInstance parent, ZoneInstance[] zones) {
		this.regionId = id;
		this.parent = parent;
		this.zoneCount = zones.length;
		createZoneMap(zones);
	}

	public int getRegionId() {
		return regionId;
	}

	public WorldMapInstance getParent() {
		return parent;
	}

	public Map<Integer, VisibleObject> getObjects() {
		return objects;
	}

	public MapRegion[] getNeighbours() {
		return neighbours;
	}

	/**
	 * Add neighbour region to this region neighbours list.
	 */
	void addNeighbourRegion(MapRegion neighbour) {
		neighbours = ArrayUtils.add(neighbours, neighbour);
	}

	/**
	 * Add AionObject to this region objects list.
	 */
	void add(VisibleObject object) {
		if (objects.put(object.getObjectId(), object) == null) {
			if (object instanceof Player)
				checkActiveness(playerCount.incrementAndGet() > 0);
		}
	}

	/**
	 * Remove AionObject from region objects list.
	 */
	void remove(VisibleObject object) {
		if (objects.remove(object.getObjectId()) != null)
			if (object instanceof Player) {
				checkActiveness(playerCount.decrementAndGet() > 0);
			}
	}

	private void checkActiveness(boolean active) {
		if (active && regionActive.compareAndSet(false, true)) {
			startActivation();
		} else if (!active && !parent.getParent().isInstanceType() && parent.getMapId() != 400030000) {
			startDeactivation();
		}
	}

	private void startActivation() {
		ThreadPoolManager.getInstance().schedule(() -> {
			log.debug("Activating in map {} region {}", getParent().getMapId(), regionId);
			MapRegion.this.activateObjects();
			for (MapRegion neighbor : getNeighbours()) {
				neighbor.activate();
			}
		}, 1000);
	}

	private void startDeactivation() {
		ThreadPoolManager.getInstance().schedule(() -> {
			log.debug("Deactivating inactive regions around region {} on map {} [{}]", regionId, getParent().getMapId(), getParent().getInstanceId());
			List<Creature> walkers = new ArrayList<>();
			for (MapRegion neighbor : getNeighbours()) {
				if (!neighbor.isNeighboursActive() && neighbor.deactivate()) {
					for (VisibleObject o : neighbor.getObjects().values()) {
						if (o instanceof Creature creature && creature.getAi().getState() == AIState.WALKING)
							walkers.add(creature);
					}
				}
			}
			walkers.removeIf(w -> w.getPosition().isMapRegionActive());
			if (walkers.size() > 2) { // small threshold to accommodate the walkers near the borders of deactivated regions
				String npcs = walkers.stream().map(VisibleObject::toString).collect(Collectors.joining("\n"));
				log.warn("There are {} objects walking on inactive map {} [{}]:\n{}", walkers.size(), getParent().getMapId(), getParent().getInstanceId(),
					npcs);
			}
		}, 60000);
	}

	public boolean activate() {
		if (regionActive.compareAndSet(false, true)) {
			activateObjects();
			return true;
		}
		return false;
	}

	/**
	 * Send ACTIVATE event to all objects with AI
	 */
	private void activateObjects() {
		for (VisibleObject visObject : objects.values()) {
			if (visObject instanceof Creature creature) {
				creature.getAi().onGeneralEvent(AIEventType.ACTIVATE);
			}
		}
	}

	public boolean deactivate() {
		if (regionActive.compareAndSet(true, false)) {
			deactivateObjects();
			return true;
		}
		return false;
	}

	/**
	 * Send DEACTIVATE event to all objects with AI
	 */
	private void deactivateObjects() {
		for (VisibleObject visObject : objects.values()) {
			if (visObject instanceof Creature creature && !(SiegeConfig.BALAUR_AUTO_ASSAULT && visObject instanceof SiegeNpc) && !creature.isFlag()
				&& !creature.isRaidMonster()) {
				creature.getAi().onGeneralEvent(AIEventType.DEACTIVATE);
			}
		}
	}

	public boolean isActive() {
		return regionActive.get();
	}

	private boolean isNeighboursActive() {
		for (MapRegion r : neighbours) {
			if (r.regionActive.get() && r.playerCount.get() > 0)
				return true;
		}
		return false;
	}

	public void revalidateZones(Creature creature) {
		for (Entry<Integer, TreeSet<ZoneInstance>> e : zoneMap.entrySet()) {
			boolean foundZone = false;
			int category = e.getKey();
			TreeSet<ZoneInstance> zones = e.getValue();
			for (ZoneInstance zone : zones) {
				if (!creature.isSpawned() || (category != -1 && foundZone)) {
					zone.onLeave(creature);
					continue;
				}
				if (!zone.revalidate(creature)) {
					zone.onLeave(creature);
					continue;
				}
				if (category != -1) {
					foundZone = true;
				}
				zone.onEnter(creature);
			}
		}
	}

	public List<ZoneInstance> findZones(Creature creature) {
		List<ZoneInstance> z = new ArrayList<>();
		for (Entry<Integer, TreeSet<ZoneInstance>> e : zoneMap.entrySet()) {
			TreeSet<ZoneInstance> zones = e.getValue();
			for (ZoneInstance zone : zones) {
				if (zone.isInsideCreature(creature)) {
					z.add(zone);
				}
			}
		}
		return z;
	}

	public boolean onDie(Creature attacker, Creature target) {
		for (Entry<Integer, TreeSet<ZoneInstance>> e : zoneMap.entrySet()) {
			TreeSet<ZoneInstance> zones = e.getValue();
			for (ZoneInstance zone : zones) {
				if (zone.isInsideCreature(target)) {
					if (zone.onDie(attacker, target))
						return true;
				}
			}
		}
		return false;
	}

	public boolean isInsideZone(ZoneName zoneName, float x, float y, float z) {
		for (Entry<Integer, TreeSet<ZoneInstance>> e : zoneMap.entrySet()) {
			TreeSet<ZoneInstance> zones = e.getValue();
			for (ZoneInstance zone : zones) {
				if (zone.getZoneTemplate().getName() != zoneName)
					continue;
				return zone.isInsideCordinate(x, y, z);
			}
		}
		return false;
	}

	public boolean isInsideZone(ZoneName zoneName, Creature creature) {
		for (Entry<Integer, TreeSet<ZoneInstance>> e : zoneMap.entrySet()) {
			TreeSet<ZoneInstance> zones = e.getValue();
			for (ZoneInstance zone : zones) {
				if (zone.getZoneTemplate().getName() == zoneName)
					return zone.isInsideCreature(creature);
			}
		}
		return false;
	}

	/**
	 * Item use zones always have the same names instances, while we have unique names; Thus, a special check for item use.
	 */
	public boolean isInsideItemUseZone(ZoneName zoneName, Creature creature) {
		boolean checkFortresses = "_ABYSS_CASTLE_AREA_".equals(zoneName.name()); // some items have this special zonename in uselimits
		for (Entry<Integer, TreeSet<ZoneInstance>> e : zoneMap.entrySet()) {
			TreeSet<ZoneInstance> zones = e.getValue();
			for (ZoneInstance zone : zones) {
				if (checkFortresses) {
					if (!zone.getZoneTemplate().getZoneType().equals(ZoneClassName.FORT))
						continue;
				} else {
					if (!zone.getZoneTemplate().getXmlName().startsWith(zoneName.toString()))
						continue;
				}
				if (zone.isInsideCreature(creature))
					return true;
			}
		}
		return false;
	}

	private void createZoneMap(ZoneInstance[] zones) {
		zoneMap = new LinkedHashMap<>();
		for (ZoneInstance zone : zones) {
			int category = -1;
			if (zone.getZoneTemplate().getPriority() != 0) {
				category = zone.getZoneTemplate().getZoneType().ordinal();
			}
			zoneMap.computeIfAbsent(category, k -> new TreeSet<>()).add(zone);
		}
	}

	public int getZoneCount() {
		return zoneCount;
	}
}
