package com.aionemu.gameserver.world;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.WorldConfig;
import com.aionemu.gameserver.instance.handlers.InstanceHandler;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.GeneralTeam;
import com.aionemu.gameserver.model.templates.quest.QuestNpc;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.model.templates.zone.ZoneClassName;
import com.aionemu.gameserver.model.templates.zone.ZoneType;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.collections.CollectionUtil;
import com.aionemu.gameserver.world.exceptions.DuplicateAionObjectException;
import com.aionemu.gameserver.world.zone.RegionZone;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;
import com.aionemu.gameserver.world.zone.ZoneService;

/**
 * World map instance object.
 * 
 * @author -Nemesiss-
 */
public abstract class WorldMapInstance implements Iterable<VisibleObject> {

	private static final Logger log = LoggerFactory.getLogger(WorldMapInstance.class);
	public static final int regionSize = WorldConfig.WORLD_REGION_SIZE;

	private final WorldMap parent;
	protected final Map<Integer, MapRegion> regions = new HashMap<>();
	private final Map<Integer, VisibleObject> worldMapObjects = new ConcurrentHashMap<>(); // All objects spawned in this world map instance
	private final Map<Integer, Npc> worldMapNpcs = new ConcurrentHashMap<>(); // All npcs spawned in this world map instance
	private final Map<Integer, Player> worldMapPlayers = new ConcurrentHashMap<>(); // All players spawned in this world map instance
	private final Set<Integer> registeredObjects = ConcurrentHashMap.newKeySet();
	private final Set<Integer> questIds = ConcurrentHashMap.newKeySet();
	private final Map<ZoneName, ZoneInstance> zones;
	private final InstanceHandler instanceHandler;
	private final int instanceId; // Id of this instance (channel)
	private final int maxPlayers;
	private WorldPosition startPos;
	private long lastPlayerLeaveTime;
	private GeneralTeam<?, ?> registeredTeam;
	private Future<?> emptyInstanceTask, updateNearbyQuestsTask;

	public WorldMapInstance(WorldMap parent, int instanceId, int maxPlayers, Function<WorldMapInstance, InstanceHandler> instanceHandlerSupplier) {
		this.parent = parent;
		this.zones = ZoneService.getInstance().getZoneInstancesByWorldId(parent.getMapId());
		this.instanceHandler = instanceHandlerSupplier.apply(this);
		this.instanceId = instanceId;
		this.maxPlayers = maxPlayers;
		initMapRegions();
	}

	public int getMapId() {
		return getParent().getMapId();
	}

	public WorldMap getParent() {
		return parent;
	}

	public WorldMapTemplate getTemplate() {
		return parent.getTemplate();
	}

	/**
	 * Returns MapRegion that contains coordinates of VisibleObject. If the region doesn't exist, it's created.
	 * 
	 * @return a MapRegion
	 */
	MapRegion getRegion(VisibleObject object) {
		return getRegion(object.getX(), object.getY(), object.getZ());
	}

	/**
	 * Returns MapRegion that contains given x,y coordinates. If the region doesn't exist, it's created.
	 * 
	 * @return a MapRegion
	 */
	public abstract MapRegion getRegion(float x, float y, float z);

	/**
	 * Create new MapRegion and add link to neighbours.
	 * 
	 * @return newly created map region
	 */
	protected abstract MapRegion createMapRegion(int regionId);

	protected abstract void initMapRegions();

	public abstract boolean isPersonal();

	public abstract int getOwnerId();

	public void addObject(VisibleObject object) {
		if (worldMapObjects.putIfAbsent(object.getObjectId(), object) != null) {
			throw new DuplicateAionObjectException(object, worldMapObjects.get(object.getObjectId()));
		}
		if (object instanceof Npc) {
			worldMapNpcs.put(object.getObjectId(), (Npc) object);
			QuestNpc qNpc = QuestEngine.getInstance().getQuestNpc(object.getObjectTemplate().getTemplateId());
			if (qNpc != null) {
				boolean updateNearbyQuests = false;
				for (int id : qNpc.getOnQuestStart()) {
					if (questIds.add(id)) {
						updateNearbyQuests = true;
					}
				}
				if (updateNearbyQuests && updateNearbyQuestsTask == null) { // delayed with null check to prevent packet spam on multispawns (bases, siege,
																																		// ...)
					updateNearbyQuestsTask = ThreadPoolManager.getInstance().schedule(() -> {
						updateNearbyQuestsTask = null;
						forEachPlayer(player -> player.getController().updateNearbyQuests());
					}, 1500);
				}
			}
		}
		if (object instanceof Player player) {
			if (getParent().isFlightAllowed())
				player.setInsideZoneType(ZoneType.FLY);
			worldMapPlayers.put(object.getObjectId(), player);
		}
	}

	public void removeObject(AionObject object) {
		worldMapObjects.remove(object.getObjectId());
		if (object instanceof Player player) {
			lastPlayerLeaveTime = System.currentTimeMillis();
			if (getParent().isFlightAllowed()) {
				player.unsetInsideZoneType(ZoneType.FLY);
				// necessary for fly maps like the abyss (they don't have FlyZones, so no FlyZoneInstance.onLeave() is called)
				player.getController().onLeaveFlyArea();
			}
			worldMapPlayers.remove(object.getObjectId());
		} else if (object instanceof Npc) {
			worldMapNpcs.remove(object.getObjectId());
		}
	}

	public int getPlayerCount() {
		return worldMapPlayers.size();
	}

	public List<Player> getPlayersInside() {
		return new ArrayList<>(worldMapPlayers.values());
	}

	/**
	 * Gets the player with the given object id in this world map instance
	 * 
	 * @param objId
	 * @return Player or null if there is no player with this id on the map instance
	 */
	public Player getPlayer(int objId) {
		return worldMapPlayers.get(objId);
	}

	public VisibleObject getObject(int objId) {
		return worldMapObjects.get(objId);
	}

	public Npc getNpc(int npcId) {
		for (Npc npc : worldMapNpcs.values()) {
			if (npc != null && npc.getNpcId() == npcId)
				return npc;
		}
		return null;
	}

	public List<Npc> getNpcs(int... npcIds) {
		List<Npc> npcs = new ArrayList<>();
		forEachNpc(npc -> {
			for (int npcId : npcIds) {
				if (npc.getNpcId() == npcId)
					npcs.add(npc);
			}
		});
		return npcs;
	}

	public List<Npc> getNpcs() {
		List<Npc> npcs = new ArrayList<>();
		forEachNpc(npcs::add);
		return npcs;
	}

	public VisibleObject getObjectByStaticId(int staticId) {
		return worldMapObjects.values().stream().filter(o -> o != null && o.getSpawn() != null && o.getSpawn().getStaticId() == staticId).findAny().orElse(null);
	}

	public int getInstanceId() {
		return instanceId;
	}

	public final boolean isBeginnerInstance() {
		if (parent == null)
			return false;

		if (parent.getTemplate().isInstance()) {
			// TODO: check Karamatis and Ataxiar for exception in FastTrack ?
			// return parent.getTemplate().getBeginnerTwinCount() > 0;
			return false;
		}

		int twinCount = parent.getTemplate().getTwinCount();
		if (twinCount == 0)
			twinCount = 1;
		return getInstanceId() > twinCount;
	}

	public void registerTeam(GeneralTeam<?, ?> team) {
		if (registeredTeam != null)
			throw new IllegalStateException("A team for instance " + instanceId + " of map " + getMapId() + " is already registered");
		registeredTeam = team;
		register(team.getTeamId());
	}

	public void register(int objectId) {
		registeredObjects.add(objectId);
	}

	public Set<Integer> getRegisteredObjects() {
		return registeredObjects;
	}

	/**
	 * @return Count of all registered objects with this instance. Since objects can be players or teams, it does not resemble registered player count.
	 */
	public int getRegisteredCount() {
		return registeredObjects.size();
	}

	public boolean isRegistered(int objectId) {
		return registeredObjects.contains(objectId);
	}

	public Future<?> getEmptyInstanceTask() {
		return emptyInstanceTask;
	}

	public void setEmptyInstanceTask(Future<?> emptyInstanceTask) {
		this.emptyInstanceTask = emptyInstanceTask;
	}

	public GeneralTeam<?, ?> getRegisteredTeam() {
		return registeredTeam;
	}

	public Set<Integer> getQuestIds() {
		return questIds;
	}

	public final InstanceHandler getInstanceHandler() {
		return instanceHandler;
	}

	public void setStartPos(WorldPosition startPos) {
		this.startPos = startPos;
	}

	public WorldPosition getStartPos() {
		return startPos;
	}

	protected ZoneInstance[] filterZones(int mapId, int regionId, float startX, float startY, float minZ, float maxZ) {
		RegionZone regionZone = new RegionZone(startX, startY, minZ, maxZ);
		return zones.values().stream().filter(zoneInstance -> {
			if (zoneInstance.getAreaTemplate().intersectsRectangle(regionZone))
				return true;
			if (zoneInstance.getZoneTemplate().getZoneType() == ZoneClassName.DUMMY)
				log.error("Region " + regionId + " should intersect with whole map zone!!! (map=" + mapId + ")");
			return false;
		}).toArray(ZoneInstance[]::new);
	}

	public boolean isInsideZone(VisibleObject object, ZoneName zoneName) {
		ZoneInstance zoneTemplate = zones.get(zoneName);
		return zoneTemplate != null && isInsideZone(object.getPosition(), zoneName);
	}

	public boolean isInsideZone(WorldPosition pos, ZoneName zoneName) {
		MapRegion mapRegion = this.getRegion(pos.getX(), pos.getY(), pos.getZ());
		return mapRegion.isInsideZone(zoneName, pos.getX(), pos.getY(), pos.getZ());
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public boolean isFull() {
		return maxPlayers > 0 && getPlayerCount() >= maxPlayers;
	}

	public long getLastPlayerLeaveTime() {
		return lastPlayerLeaveTime;
	}

	public void setDoorState(int staticId, boolean open) {
		for (VisibleObject v : worldMapObjects.values()) {
			if (v instanceof StaticDoor staticDoor && v.getSpawn().getStaticId() == staticId) {
				staticDoor.setOpen(open);
				return;
			}
		}
		log.warn("Door (ID: " + staticId + ") doesn't exist", new RuntimeException());
	}

	public Iterator<VisibleObject> iterator() {
		return worldMapObjects.values().iterator();
	}

	public void forEachNpc(Consumer<Npc> consumer) {
		CollectionUtil.forEach(worldMapNpcs.values(), consumer);
	}

	public void forEachPlayer(Consumer<Player> consumer) {
		CollectionUtil.forEach(worldMapPlayers.values(), consumer);
	}

	public void forEachDoor(Consumer<StaticDoor> consumer) {
		CollectionUtil.forEach(worldMapObjects.values(), o -> {
			if (o instanceof StaticDoor staticDoor)
				consumer.accept(staticDoor);
		});
	}

	@Override
	public String toString() {
		return "WorldMapInstance " + getMapId() + " [" + instanceId + "]";
	}
}
