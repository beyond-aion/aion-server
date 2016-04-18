package com.aionemu.gameserver.world;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.GenericValidator;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.PlayerInitialData.LocationData;
import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.BindPointPosition;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.world.container.PlayerContainer;
import com.aionemu.gameserver.world.exceptions.AlreadySpawnedException;
import com.aionemu.gameserver.world.exceptions.DuplicateAionObjectException;
import com.aionemu.gameserver.world.knownlist.Visitor;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * World object for storing and spawning, despawning etc players and other in-game objects. It also manage WorldMaps and instances.
 * 
 * @author -Nemesiss-, Source, Wakizashi
 */
public class World {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(World.class);

	/**
	 * Container with all players that entered world.
	 */
	private final PlayerContainer allPlayers;

	/**
	 * Container with all AionObjects in the world [ie Players, Npcs etc]
	 */
	private final ConcurrentHashMap<Integer, VisibleObject> allObjects;

	/**
	 * Container with all SiegeNpcs in the world [SiegeNpcs,SiegeProtectors etc]
	 */
	private final TIntObjectHashMap<Collection<SiegeNpc>> localSiegeNpcs = new TIntObjectHashMap<>();

	/**
	 * Container with all Npcs related to base spawns
	 */
	private final ConcurrentHashMap<Integer, List<Npc>> baseNpc;

	/**
	 * Container with all Npcs in the world
	 */
	private final ConcurrentHashMap<Integer, Npc> allNpcs;

	/**
	 * World maps supported by server.
	 */
	private final TIntObjectHashMap<WorldMap> worldMaps;

	/**
	 * Constructor.
	 */
	private World() {
		allPlayers = new PlayerContainer();
		allObjects = new ConcurrentHashMap<>();
		baseNpc = new ConcurrentHashMap<>();
		allNpcs = new ConcurrentHashMap<>();
		worldMaps = new TIntObjectHashMap<>();

		for (WorldMapTemplate template : DataManager.WORLD_MAPS_DATA)
			worldMaps.put(template.getMapId(), new WorldMap(template));

		log.info("World: " + worldMaps.size() + " world maps created.");
	}

	public static World getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * Store object in the world.
	 * 
	 * @param object
	 */
	public void storeObject(VisibleObject object) {
		if (object.getPosition() == null) {
			log.warn("Not putting object with null position!!! " + object.getObjectTemplate().getTemplateId());
			return;
		}
		if (allObjects.put(object.getObjectId(), object) != null)
			throw new DuplicateAionObjectException();

		if (object instanceof Player) {
			allPlayers.add((Player) object);
		}

		if (object instanceof SiegeNpc) {
			SiegeNpc siegeNpc = (SiegeNpc) object;
			Collection<SiegeNpc> npcs = localSiegeNpcs.get(siegeNpc.getSiegeId());
			if (npcs == null) {
				synchronized (localSiegeNpcs) {
					if (localSiegeNpcs.containsKey(siegeNpc.getSiegeId())) {
						npcs = localSiegeNpcs.get(siegeNpc.getSiegeId());
					} else {
						// We now have multi-threaded siege timers
						// This should be thread-safe
						npcs = new CopyOnWriteArrayList<>();
						localSiegeNpcs.put(siegeNpc.getSiegeId(), npcs);
					}
				}
			}

			npcs.add(siegeNpc);
		}

		if (object.getSpawn() instanceof BaseSpawnTemplate) {
			BaseSpawnTemplate bst = (BaseSpawnTemplate) object.getSpawn();
			int baseId = bst.getId();
			if (!baseNpc.containsKey(baseId)) {
				baseNpc.putIfAbsent(baseId, new CopyOnWriteArrayList<Npc>());
			}
			baseNpc.get(baseId).add((Npc) object);
		}

		if (object instanceof Npc) {
			allNpcs.put(object.getObjectId(), (Npc) object);
		}
	}

	/**
	 * Remove Object from the world.<br>
	 * If the given object is Npc then it also releases it's objId from IDFactory.
	 * 
	 * @param object
	 */
	public void removeObject(VisibleObject object) {
		allObjects.remove(object.getObjectId());

		if (object instanceof SiegeNpc) {
			SiegeNpc siegeNpc = (SiegeNpc) object;
			Collection<SiegeNpc> locSpawn = localSiegeNpcs.get(siegeNpc.getSiegeId());
			if (!GenericValidator.isBlankOrNull(locSpawn)) {
				locSpawn.remove(siegeNpc);
			}
		}

		if (object.getSpawn() instanceof BaseSpawnTemplate) {
			BaseSpawnTemplate bst = (BaseSpawnTemplate) object.getSpawn();
			int baseId = bst.getId();
			baseNpc.get(baseId).remove(object);
		}

		if (object instanceof Npc) {
			allNpcs.remove(object.getObjectId());
		}

		if (object instanceof Player) {
			allPlayers.remove((Player) object);
		}
	}

	/**
	 * Returns Players iterator.
	 * 
	 * @return Players iterator.
	 */
	public Iterator<Player> getPlayersIterator() {
		return allPlayers.iterator();
	}

	public Collection<SiegeNpc> getLocalSiegeNpcs(int locationId) {
		Collection<SiegeNpc> result = localSiegeNpcs.get(locationId);
		return result != null ? result : Collections.<SiegeNpc> emptySet();
	}

	public List<Npc> getBaseSpawns(int baseId) {
		return baseNpc.get(baseId);
	}

	public Collection<Npc> getNpcs() {
		return allNpcs.values();
	}

	/**
	 * Finds player by player name.
	 * 
	 * @param name
	 *          - name of player
	 * @return Player
	 */
	public Player findPlayer(String name) {
		return allPlayers.get(name);
	}

	/**
	 * Finds player by player objectId.
	 * 
	 * @param objectId
	 *          - objectId of player
	 * @return Player
	 */
	public Player findPlayer(int objectId) {
		return allPlayers.get(objectId);
	}

	/**
	 * Finds VisibleObject by objectId.
	 * 
	 * @param objectId
	 *          - objectId of AionObject or null if not in world
	 * @return VisibleObject
	 */
	public VisibleObject findVisibleObject(int objectId) {
		return allObjects.get(objectId);
	}

	/**
	 * Finds Npc by objectId.
	 * 
	 * @param objectId
	 *          - objectId of Npc
	 * @return Npc or null if not in world or object ID belongs to other AionObject type in world.
	 */
	public Npc findNpc(int objectId) {
		return findVisibleObject(objectId) instanceof Npc ? (Npc) findVisibleObject(objectId) : null;
	}

	/**
	 * Check whether object is in world
	 * 
	 * @param objectId
	 * @return
	 */
	public boolean isInWorld(int objectId) {
		return allObjects.containsKey(objectId);
	}

	/**
	 * Check whether object is in world
	 * 
	 * @param object
	 * @return
	 */
	public boolean isInWorld(VisibleObject object) {
		return isInWorld(object.getObjectId());
	}

	/**
	 * Return World Map by id
	 * 
	 * @param id
	 * @return World map, null if it doesn't exist.
	 */
	public WorldMap getWorldMap(int id) {
		return worldMaps.get(id);
	}

	/**
	 * Update position of VisibleObject [used when object is moving on one map instance]. Check if active map region changed and do all needed updates.
	 * 
	 * @param object
	 * @param newX
	 * @param newY
	 * @param newZ
	 * @param newHeading
	 */
	public void updatePosition(VisibleObject object, float newX, float newY, float newZ, byte newHeading) {
		if (object instanceof Player) {
			Player player = (Player) object;
			if (player.isProtectionActive() && (player.getX() != newX || player.getY() != newY || player.getZ() > newZ + 0.5f)) {
				((Player) object).getController().stopProtectionActiveTask();
			}
		}
		this.updatePosition(object, newX, newY, newZ, newHeading, true);
	}

	/**
	 * @param object
	 * @param newX
	 * @param newY
	 * @param newZ
	 * @param newHeading
	 */
	public void updatePosition(VisibleObject object, float newX, float newY, float newZ, byte newHeading, boolean updateKnownList) {
		// prevent updating object position in despawned state
		if (!object.isSpawned())
			return;

		MapRegion oldRegion = object.getActiveRegion();
		if (oldRegion == null) {
			log.warn(String.format("CHECKPOINT: oldRegion is null, map - %d, object coordinates - %f %f %f", object.getWorldId(), object.getX(),
				object.getY(), object.getZ()));
			return;
		}

		MapRegion newRegion = oldRegion.getParent().getRegion(newX, newY, newZ);
		if (newRegion == null) {
			log.warn(String.format("CHECKPOINT: newRegion is null, map - %d, object coordinates - %f %f %f", object.getWorldId(), newX, newY, newZ),
				new Throwable());
			if (object instanceof Creature)
				((Creature) object).getMoveController().abortMove();
			if (object instanceof Player) {
				Player player = (Player) object;
				float x, y, z;
				int worldId;
				byte h = 0;

				if (player.getBindPoint() != null) {
					BindPointPosition bplist = player.getBindPoint();
					worldId = bplist.getMapId();
					x = bplist.getX();
					y = bplist.getY();
					z = bplist.getZ();
					h = bplist.getHeading();
				} else {
					LocationData locationData = DataManager.PLAYER_INITIAL_DATA.getSpawnLocation(player.getCommonData().getRace());
					worldId = locationData.getMapId();
					x = locationData.getX();
					y = locationData.getY();
					z = locationData.getZ();
				}
				setPosition(object, worldId, x, y, z, h);
			}
			return;
		}

		object.getPosition().setXYZH(newX, newY, newZ, newHeading);

		if (newRegion != oldRegion) {
			if (object instanceof Creature) {
				oldRegion.revalidateZones((Creature) object);
				newRegion.revalidateZones((Creature) object);
			}
			oldRegion.remove(object);
			newRegion.add(object);
			object.getPosition().setMapRegion(newRegion);
		}

		if (updateKnownList)
			object.updateKnownlist();
	}

	/**
	 * Set position of VisibleObject without spawning [object will be invisible]. If object is spawned it will be despawned first.
	 * 
	 * @return True, if position was set correctly.
	 */
	public boolean setPosition(VisibleObject object, int mapId, float x, float y, float z, byte heading) {
		int instanceId = 1;
		if (object.getPosition() != null && object.getPosition().getMapId() == mapId)
			instanceId = object.getInstanceId();
		return setPosition(object, mapId, instanceId, x, y, z, heading);
	}

	/**
	 * Set position of VisibleObject without spawning [object will be invisible]. If object is spawned it will be despawned first.
	 * 
	 * @return True, if position was set correctly.
	 */
	public boolean setPosition(VisibleObject object, int mapId, int instance, float x, float y, float z, byte heading) {
		if (object == null)
			return false;
		WorldPosition pos = createPosition(mapId, x, y, z, heading, instance);
		if (pos == null)
			return false;
		if (object.isSpawned())
			despawn(object);
		object.setPosition(pos);
		return true;
	}

	/**
	 * Creates and return {@link WorldPosition} object, representing position with given parameters.
	 * 
	 * @param mapId
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param instanceId
	 * @return WorldPosition
	 */
	public WorldPosition createPosition(int mapId, float x, float y, float z, byte heading, int instanceId) {
		WorldMap map = getWorldMap(mapId);
		if (map == null || map.getWorldMapInstanceById(instanceId) == null)
			return null;
		MapRegion mr = map.getWorldMapInstanceById(instanceId).getRegion(x, y, z);
		if (mr == null)
			log.warn("MapRegion should not be null (mapId=" + mapId + ", x=" + x + ", y=" + y + ", z=" + z + ", instanceId=" + instanceId + ")",
				new NullPointerException()); // don't actually throw the exception (we only want to print the stack trace)
		return new WorldPosition(mapId, x, y, z, heading, mr);
	}

	/**
	 * Spawns the object at the current position (use setPosition). Object will be visible by others and will see other objects.
	 * 
	 * @param object
	 * @throws AlreadySpawnedException
	 *           when object is already spawned.
	 */
	public void spawn(VisibleObject object) {
		spawn(object, true);
	}

	/**
	 * Spawns the object at the current position (use setPosition). Object will be visible by others and will see other objects, if updateKnownlist is
	 * set to true.
	 * 
	 * @param object
	 * @param updateKnownlist
	 * @throws AlreadySpawnedException
	 *           when object is already spawned.
	 */
	public void spawn(VisibleObject object, boolean updateKnownlist) {
		if (object == null)
			return;
		if (object.getPosition().isSpawned())
			throw new AlreadySpawnedException();

		object.getController().onBeforeSpawn();
		object.getPosition().setIsSpawned(true);

		object.getActiveRegion().getParent().addObject(object);
		object.getActiveRegion().add(object);
		object.getController().onAfterSpawn();

		if (updateKnownlist)
			object.updateKnownlist();
	}

	/**
	 * Despawn VisibleObject, object will become invisible and object position will become invalid. All others objects will be noticed that this object
	 * is no longer visible.
	 * 
	 * @throws NullPointerException
	 *           if object is already despawned
	 */
	public void despawn(VisibleObject object) {
		despawn(object, ObjectDeleteAnimation.FADE_OUT);
	}

	public void despawn(VisibleObject object, ObjectDeleteAnimation animation) {
		MapRegion oldMapRegion = object.getActiveRegion();
		if (oldMapRegion != null) { // can be null if an instance gets deleted?
			if (oldMapRegion.getParent() != null)
				oldMapRegion.getParent().removeObject(object);
			oldMapRegion.remove(object);
		}
		object.getPosition().setIsSpawned(false);
		if (oldMapRegion != null && object instanceof Creature) {
			oldMapRegion.revalidateZones((Creature) object);
		}
		object.clearKnownlist(animation);
	}

	/**
	 * @return
	 */
	public Collection<Player> getAllPlayers() {
		return allPlayers.getAllPlayers();
	}

	/**
	 * @param visitor
	 */
	public void doOnAllPlayers(Visitor<Player> visitor) {
		allPlayers.doOnAllPlayers(visitor);
	}

	/**
	 * @param visitor
	 */
	public void doOnAllObjects(Visitor<VisibleObject> visitor) {
		try {
			for (ConcurrentHashMap.Entry<Integer, VisibleObject> e : allObjects.entrySet()) {
				VisibleObject object = e.getValue();
				if (object != null) {
					visitor.visit(object);
				}
			}
		} catch (Exception ex) {
			log.error("Exception when running visitor on all objects", ex);
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		protected static final World instance = new World();
	}

}
