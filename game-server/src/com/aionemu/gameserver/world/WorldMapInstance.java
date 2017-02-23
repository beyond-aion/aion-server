package com.aionemu.gameserver.world;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.WorldConfig;
import com.aionemu.gameserver.instance.handlers.InstanceHandler;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.model.team.league.League;
import com.aionemu.gameserver.model.templates.quest.QuestNpc;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.model.templates.zone.ZoneClassName;
import com.aionemu.gameserver.model.templates.zone.ZoneType;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.exceptions.DuplicateAionObjectException;
import com.aionemu.gameserver.world.zone.RegionZone;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;
import com.aionemu.gameserver.world.zone.ZoneService;

import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastSet;
import javolution.util.FastTable;

/**
 * World map instance object.
 * 
 * @author -Nemesiss-
 */
public abstract class WorldMapInstance implements Iterable<VisibleObject> {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(WorldMapInstance.class);
	/**
	 * Size of region
	 */
	public static final int regionSize = WorldConfig.WORLD_REGION_SIZE;
	/**
	 * WorldMap which is parent of this instance.
	 */
	private final WorldMap parent;
	/**
	 * Map of active regions.
	 */
	protected final TIntObjectHashMap<MapRegion> regions = new TIntObjectHashMap<>();
	/**
	 * All objects spawned in this world map instance
	 */
	private final Map<Integer, VisibleObject> worldMapObjects = new ConcurrentHashMap<>();
	/**
	 * All players spawned in this world map instance
	 */
	private final Map<Integer, Player> worldMapPlayers = new ConcurrentHashMap<>();
	private final Set<Integer> registeredObjects = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
	private PlayerGroup registeredGroup = null;
	private Future<?> emptyInstanceTask = null;
	private Future<?> updateNearbyQuestsTask = null;
	/**
	 * Id of this instance (channel)
	 */
	private int instanceId;
	private final Set<Integer> questIds = new FastSet<>();
	private InstanceHandler instanceHandler;
	private Map<ZoneName, ZoneInstance> zones = new HashMap<>();
	// TODO: Merge this with owner
	private int soloPlayer;
	private PlayerAlliance registredAlliance;
	private League registredLeague;
	private float[] startPos;
	private int playerSize;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 */
	public WorldMapInstance(WorldMap parent, int instanceId) {
		this.parent = parent;
		this.instanceId = instanceId;
		this.zones = ZoneService.getInstance().getZoneInstancesByWorldId(parent.getMapId());
		initMapRegions();
	}

	/**
	 * Return World map id.
	 * 
	 * @return world map id
	 */
	public Integer getMapId() {
		return getParent().getMapId();
	}

	/**
	 * Returns WorldMap which is parent of this instance
	 * 
	 * @return parent
	 */
	public WorldMap getParent() {
		return parent;
	}

	public WorldMapTemplate getTemplate() {
		return parent.getTemplate();
	}

	/**
	 * Returns MapRegion that contains coordinates of VisibleObject. If the region doesn't exist, it's created.
	 * 
	 * @param object
	 * @return a MapRegion
	 */
	MapRegion getRegion(VisibleObject object) {
		return getRegion(object.getX(), object.getY(), object.getZ());
	}

	/**
	 * Returns MapRegion that contains given x,y coordinates. If the region doesn't exist, it's created.
	 * 
	 * @param x
	 * @param y
	 * @return a MapRegion
	 */
	public abstract MapRegion getRegion(float x, float y, float z);

	/**
	 * Create new MapRegion and add link to neighbours.
	 * 
	 * @param regionId
	 * @return newly created map region
	 */
	protected abstract MapRegion createMapRegion(int regionId);

	protected abstract void initMapRegions();

	public abstract boolean isPersonal();

	public abstract int getOwnerId();

	/**
	 * @param object
	 */
	public void addObject(VisibleObject object) {
		if (worldMapObjects.putIfAbsent(object.getObjectId(), object) != null) {
			throw new DuplicateAionObjectException(object + " already present:\n" + worldMapObjects.get(object.getObjectId()));
		}
		if (object instanceof Npc) {
			QuestNpc qNpc = QuestEngine.getInstance().getQuestNpc(object.getObjectTemplate().getTemplateId());
			if (qNpc != null) {
				boolean updateNearbyQuests = false;
				for (int id : qNpc.getOnQuestStart()) {
					if (!questIds.contains(id)) {
						questIds.add(id);
						updateNearbyQuests = true;
					}
				}
				if (updateNearbyQuests && updateNearbyQuestsTask == null) { // delayed with null check to prevent packet spam on multispawns (bases, siege, ...)
					updateNearbyQuestsTask = ThreadPoolManager.getInstance().schedule(() -> {
						updateNearbyQuestsTask = null;
						forEachPlayer(player -> player.getController().updateNearbyQuests());
					}, 1500);
				}
			}
		}
		if (object instanceof Player) {
			if (this.getParent().isPossibleFly())
				((Player) object).setInsideZoneType(ZoneType.FLY);
			worldMapPlayers.put(object.getObjectId(), (Player) object);
		}
	}

	/**
	 * @param object
	 */
	public void removeObject(AionObject object) {
		worldMapObjects.remove(object.getObjectId());
		if (object instanceof Player) {
			if (this.getParent().isPossibleFly()) {
				((Player) object).unsetInsideZoneType(ZoneType.FLY);
				// necessary for fly maps like the abyss (they don't have FlyZones, so no FlyZoneInstance.onLeave() is called)
				((Player) object).getController().onLeaveFlyArea();
			}
			worldMapPlayers.remove(object.getObjectId());
		}
	}

	public int playerCount() {
		return worldMapPlayers.size();
	}

	public List<Player> getPlayersInside() {
		return FastTable.of(worldMapPlayers.values());
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
		for (VisibleObject v : this) {
			if (v instanceof Npc && v.getObjectTemplate().getTemplateId() == npcId)
				return (Npc) v;
		}
		return null;
	}

	public List<Npc> getNpcs(int npcId) {
		List<Npc> npcs = new FastTable<>();
		for (VisibleObject v : this) {
			if (v instanceof Npc && v.getObjectTemplate().getTemplateId() == npcId)
				npcs.add((Npc) v);
		}
		return npcs;
	}

	public List<Npc> getNpcs() {
		List<Npc> npcs = new FastTable<>();
		for (VisibleObject v : this) {
			if (v instanceof Npc)
				npcs.add((Npc) v);
		}
		return npcs;
	}

	public Map<Integer, StaticDoor> getDoors() {
		Map<Integer, StaticDoor> doors = new HashMap<>();
		for (VisibleObject v : this) {
			if (v instanceof StaticDoor)
				doors.put(v.getSpawn().getStaticId(), (StaticDoor) v);
		}
		return doors;
	}

	/**
	 * @return the instanceIndex
	 */
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

	public void registerGroup(PlayerGroup group, int playerSize) {
		registeredGroup = group;
		register(group.getTeamId());
		this.playerSize = playerSize;
	}

	public void registerGroup(PlayerAlliance group, int playerSize) {
		registredAlliance = group;
		register(group.getObjectId());
		this.playerSize = playerSize;
	}

	public void registerGroup(League group, int playerSize) {
		registredLeague = group;
		register(group.getObjectId());
		this.playerSize = playerSize;
	}

	public PlayerAlliance getRegistredAlliance() {
		return registredAlliance;
	}

	public League getRegistredLeague() {
		return registredLeague;
	}

	/**
	 * @param objectId
	 */
	public void register(int objectId) {
		registeredObjects.add(objectId);
	}

	public boolean isRegistered(int objectId) {
		return registeredObjects.contains(objectId);
	}

	/**
	 * @return the emptyInstanceTask
	 */
	public Future<?> getEmptyInstanceTask() {
		return emptyInstanceTask;
	}

	/**
	 * @param emptyInstanceTask
	 *          the emptyInstanceTask to set
	 */
	public void setEmptyInstanceTask(Future<?> emptyInstanceTask) {
		this.emptyInstanceTask = emptyInstanceTask;
	}

	/**
	 * @return the registeredGroup
	 */
	public PlayerGroup getRegisteredGroup() {
		return registeredGroup;
	}

	public Set<Integer> getQuestIds() {
		return questIds;
	}

	public final InstanceHandler getInstanceHandler() {
		return instanceHandler;
	}

	public final void setInstanceHandler(InstanceHandler instanceHandler) {
		this.instanceHandler = instanceHandler;
	}

	public void setStartPos(float instanceStartPosX, float instanceStartPosY, float instanceStartPosZ) {
		this.startPos = new float[] { instanceStartPosX, instanceStartPosY, instanceStartPosZ };
	}

	public float[] getStartPos() {
		return startPos;
	}

	public void forEachPlayer(Consumer<Player> function) {
		try {
			worldMapPlayers.values().forEach(player -> {
				if (player != null) // can be null if entry got removed after iterator allocation
					function.accept(player);
			});
		} catch (Exception ex) {
			log.error("Exception when iterating over players", ex);
		}
	}

	protected ZoneInstance[] filterZones(int mapId, int regionId, float startX, float startY, float minZ, float maxZ) {
		List<ZoneInstance> regionZones = new FastTable<>();
		RegionZone regionZone = new RegionZone(startX, startY, minZ, maxZ);

		for (ZoneInstance zoneInstance : zones.values()) {
			if (zoneInstance.getAreaTemplate().intersectsRectangle(regionZone))
				regionZones.add(zoneInstance);
			else if (zoneInstance.getZoneTemplate().getZoneType() == ZoneClassName.DUMMY) {
				log.error("Region " + regionId + " should intersect with whole map zone!!! (map=" + mapId + ")");
			}
		}
		return regionZones.toArray(new ZoneInstance[regionZones.size()]);
	}

	/**
	 * @param player
	 * @param zoneName
	 * @return
	 */
	public boolean isInsideZone(VisibleObject object, ZoneName zoneName) {
		ZoneInstance zoneTemplate = zones.get(zoneName);
		if (zoneTemplate == null)
			return false;
		return isInsideZone(object.getPosition(), zoneName);
	}

	/**
	 * @param pos
	 * @param zone
	 * @return
	 */
	public boolean isInsideZone(WorldPosition pos, ZoneName zoneName) {
		MapRegion mapRegion = this.getRegion(pos.getX(), pos.getY(), pos.getZ());
		return mapRegion.isInsideZone(zoneName, pos.getX(), pos.getY(), pos.getZ());
	}

	public void setSoloPlayerObjId(int obj) {
		soloPlayer = obj;
	}

	public int getSoloPlayerObj() {
		return soloPlayer;
	}

	public int getPlayerMaxSize() {
		return playerSize;
	}

	@Override
	public Iterator<VisibleObject> iterator() {
		return worldMapObjects.values().iterator();
	}
}
