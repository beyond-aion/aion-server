package com.aionemu.gameserver.world;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;

import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
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

	private static final Comparator<ZoneInstance> zoneComparator = Comparator.comparing((ZoneInstance z) -> z.getZoneTemplate().getZoneType())
		.thenComparingInt(z -> z.getZoneTemplate().getPriority())
		.thenComparingInt(z -> z.getZoneTemplate().getName().id());

	private final int regionId;
	private final WorldMapInstance parent;
	private MapRegion[] neighboursIncludingSelf = { this };
	private final ZoneInstance[] zonesSortedByTypeAndPriority;
	private final Map<Integer, VisibleObject> objects = new ConcurrentHashMap<>();
	private int playerCount;
	private boolean regionActive = false;
	private volatile boolean deactivationPending = false;

	MapRegion(int id, WorldMapInstance parent, ZoneInstance[] zones) {
		this.regionId = id;
		this.parent = parent;
		this.zonesSortedByTypeAndPriority = zones;
		Arrays.sort(zonesSortedByTypeAndPriority, zoneComparator);
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
		return neighboursIncludingSelf;
	}

	void addNeighbourRegion(MapRegion neighbour) {
		neighboursIncludingSelf = ArrayUtils.add(neighboursIncludingSelf, neighbour);
	}

	void add(VisibleObject object) {
		if (objects.put(object.getObjectId(), object) == null && object instanceof Player && incrementPlayerCount() == 1)
			activate();
	}

	void remove(VisibleObject object) {
		if (objects.remove(object.getObjectId()) instanceof Player && decrementPlayerCount() == 0)
			scheduleDeactivation();
	}

	private synchronized int getPlayerCount() {
		return playerCount;
	}

	private synchronized int incrementPlayerCount() {
		return ++playerCount;
	}

	private synchronized int decrementPlayerCount() {
		return playerCount == 0 ? 0 : --playerCount;
	}

	private synchronized boolean setRegionState(boolean active) {
		if (regionActive == active)
			return false;
		regionActive = active;
		return true;
	}

	private void activate() {
		List<MapRegion> activatedRegions = new ArrayList<>();
		for (MapRegion mapRegion : neighboursIncludingSelf) {
			if (mapRegion.setRegionState(true))
				activatedRegions.add(mapRegion);
		}
		if (!activatedRegions.isEmpty())
			ThreadPoolManager.getInstance().execute(() -> activatedRegions.forEach(r -> r.notifyCreatures(AIEventType.ACTIVATE)));
	}

	private void scheduleDeactivation() {
		if (deactivationPending)
			return;
		deactivationPending = true;
		ThreadPoolManager.getInstance().schedule(() -> {
			deactivationPending = false;
			if (getPlayerCount() == 0) {
				for (MapRegion mapRegion : neighboursIncludingSelf)
					mapRegion.tryDeactivate();
			}
		}, 60, TimeUnit.SECONDS);
	}

	private void tryDeactivate() {
		if (parent.getParent().isInstanceType() || parent.getMapId() == WorldMapType.TRANSIDIUM_ANNEX.getId())
			return;
		if (!isActive() || anyNeighbourHasPlayers())
			return;
		if (!setRegionState(false))
			return;
		notifyCreatures(AIEventType.DEACTIVATE);
	}

	private void notifyCreatures(AIEventType event) {
		for (VisibleObject visObject : objects.values()) {
			if (visObject instanceof Creature creature)
				creature.getAi().onGeneralEvent(event);
		}
	}

	public synchronized boolean isActive() {
		return regionActive;
	}

	private boolean anyNeighbourHasPlayers() {
		for (MapRegion r : neighboursIncludingSelf) {
			if (r.getPlayerCount() > 0)
				return true;
		}
		return false;
	}

	public void revalidateZones(Creature creature) {
		ZoneClassName zoneType = null;
		boolean enteredPriorityZone = false;
		for (ZoneInstance zone : zonesSortedByTypeAndPriority) {
			if (zoneType != zone.getZoneTemplate().getZoneType()) {
				zoneType = zone.getZoneTemplate().getZoneType();
				enteredPriorityZone = false;
			}
			if (!creature.isSpawned() || enteredPriorityZone || !zone.revalidate(creature)) {
				zone.onLeave(creature);
				continue;
			}
			if (zone.getZoneTemplate().getPriority() != 0) {
				enteredPriorityZone = true;
			}
			zone.onEnter(creature);
		}
	}

	public List<ZoneInstance> findZones(Creature creature) {
		List<ZoneInstance> z = new ArrayList<>();
		for (ZoneInstance zone : zonesSortedByTypeAndPriority) {
			if (zone.isInsideCreature(creature)) {
				z.add(zone);
			}
		}
		return z;
	}

	public boolean onDie(Creature attacker, Creature target) {
		for (ZoneInstance zone : zonesSortedByTypeAndPriority) {
			if (zone.isInsideCreature(target)) {
				if (zone.onDie(attacker, target))
					return true;
			}
		}
		return false;
	}

	public boolean isInsideZone(ZoneName zoneName, float x, float y, float z) {
		for (ZoneInstance zone : zonesSortedByTypeAndPriority) {
			if (zone.getZoneTemplate().getName() != zoneName)
				continue;
			return zone.isInsideCordinate(x, y, z);
		}
		return false;
	}

	public boolean isInsideZone(ZoneName zoneName, Creature creature) {
		for (ZoneInstance zone : zonesSortedByTypeAndPriority) {
			if (zone.getZoneTemplate().getName() == zoneName)
				return zone.isInsideCreature(creature);
		}
		return false;
	}

	/**
	 * Item use zones always have the same names instances, while we have unique names; Thus, a special check for item use.
	 */
	public boolean isInsideItemUseZone(ZoneName zoneName, Creature creature) {
		boolean checkFortresses = "_ABYSS_CASTLE_AREA_".equals(zoneName.name()); // some items have this special zonename in uselimits
		for (ZoneInstance zone : zonesSortedByTypeAndPriority) {
			if (checkFortresses) {
				if (zone.getZoneTemplate().getZoneType() != ZoneClassName.FORT)
					continue;
			} else if (!zone.getZoneTemplate().getXmlName().startsWith(zoneName.toString())) {
				continue;
			}
			if (zone.isInsideCreature(creature))
				return true;
		}
		return false;
	}

	public int getZoneCount() {
		return zonesSortedByTypeAndPriority.length;
	}
}
