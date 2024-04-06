package com.aionemu.gameserver.world;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.GenericValidator;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.PlayerInitialData.LocationData;
import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.BindPointPosition;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.ShieldService;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.utils.collections.CollectionUtil;
import com.aionemu.gameserver.world.container.PlayerContainer;
import com.aionemu.gameserver.world.exceptions.AlreadySpawnedException;
import com.aionemu.gameserver.world.exceptions.DuplicateAionObjectException;

/**
 * World object for storing and spawning, despawning etc players and other in-game objects. It also manage WorldMaps and instances.
 * 
 * @author -Nemesiss-, Source, Wakizashi
 */
public class World {

	private static final Logger log = LoggerFactory.getLogger(World.class);

	/**
	 * Container with all players that entered world.
	 */
	private final PlayerContainer allPlayers = new PlayerContainer();

	/**
	 * Container with all AionObjects in the world [ie Players, Npcs etc]
	 */
	private final Map<Integer, VisibleObject> allObjects = new ConcurrentHashMap<>();

	/**
	 * Container with all SiegeNpcs in the world [SiegeNpcs,SiegeProtectors etc]
	 */
	private final Map<Integer, Collection<SiegeNpc>> localSiegeNpcs = new HashMap<>();

	/**
	 * Container with all Npcs related to base spawns
	 */
	private final Map<Integer, List<Npc>> baseNpc = new ConcurrentHashMap<>();

	/**
	 * Container with all Npcs in the world
	 */
	private final Map<Integer, Npc> allNpcs = new ConcurrentHashMap<>();

	/**
	 * World maps supported by server.
	 */
	private final Map<Integer, WorldMap> worldMaps = new HashMap<>();

	private World() {
		DataManager.WORLD_MAPS_DATA.forEachParalllel(template -> {
			WorldMap worldMap = new WorldMap(template);
			synchronized (worldMaps) {
				worldMaps.put(template.getMapId(), worldMap);
			}
		});
		ShieldService.getInstance().logDetachedShields();
		log.info("World: " + worldMaps.size() + " world maps created.");
	}

	public static World getInstance() {
		return SingletonHolder.instance;
	}

	public void storeObject(VisibleObject object) {
		if (object.getPosition() == null) {
			log.error("Tried to add {} with null position in world", object);
			return;
		}
		VisibleObject oldObject = allObjects.putIfAbsent(object.getObjectId(), object);
		if (oldObject != null)
			throw new DuplicateAionObjectException(object, oldObject);

		if (object instanceof Npc npc) {
			if (object instanceof SiegeNpc siegeNpc) {
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

			if (object.getSpawn() instanceof BaseSpawnTemplate baseSpawnTemplate) {
				int baseId = baseSpawnTemplate.getId();
				if (!baseNpc.containsKey(baseId)) {
					baseNpc.putIfAbsent(baseId, new CopyOnWriteArrayList<>());
				}
				baseNpc.get(baseId).add(npc);
			}

			allNpcs.put(object.getObjectId(), npc);
		} else if (object instanceof Player player) {
			allPlayers.add(player);
		}
	}

	/**
	 * Despawns (if spawned) and completely removes the object from the world.<br>
	 * If the object was instantiated with {@code autoReleaseObjectId = true}, it's objectId will be released from IDFactory once it gets garbage
	 * collected (see {@link AionObject#AionObject(int, boolean)}).
	 * 
	 * @return True if removed, false if the object wasn't in world
	 */
	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	public boolean removeObject(VisibleObject object) {
		boolean removed = false;
		synchronized (object) {
			VisibleObject worldObject = allObjects.get(object.getObjectId());
			if (worldObject == object) {
				try {
					if (object.isSpawned())
						despawn(object);
					object.getController().onDelete();
				} catch (Exception e) {
					log.error(object + " did not leave world cleanly", e);
				}
				removed = allObjects.remove(object.getObjectId(), object);
			} else if (worldObject != null) {
				log.warn("Attempt to remove " + object + " from world but ID already belongs to " + worldObject, new Exception());
			}
		}
		if (removed) {
			if (object instanceof Npc) {
				if (object instanceof SiegeNpc siegeNpc) {
					Collection<SiegeNpc> locSpawn = localSiegeNpcs.get(siegeNpc.getSiegeId());
					if (!GenericValidator.isBlankOrNull(locSpawn)) {
						locSpawn.remove(siegeNpc);
					}
				}

				if (object.getSpawn() instanceof BaseSpawnTemplate baseSpawnTemplate) {
					int baseId = baseSpawnTemplate.getId();
					baseNpc.get(baseId).remove(object);
				}

				allNpcs.remove(object.getObjectId());
			} else if (object instanceof Player player) {
				allPlayers.remove(player);
			}
		}
		return removed;
	}

	public Collection<SiegeNpc> getLocalSiegeNpcs(int locationId) {
		Collection<SiegeNpc> result = localSiegeNpcs.get(locationId);
		return result != null ? result : Collections.emptySet();
	}

	public List<Npc> getBaseSpawns(int baseId) {
		return baseNpc.get(baseId);
	}

	public Collection<Npc> getNpcs() {
		return allNpcs.values();
	}

	/**
	 * @see PlayerContainer#get(String)
	 */
	public Player getPlayer(String name) {
		return allPlayers.get(name);
	}

	/**
	 * @see PlayerContainer#get(int)
	 */
	public Player getPlayer(int objectId) {
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

	public boolean isInWorld(int objectId) {
		return allObjects.containsKey(objectId);
	}

	public WorldMap getWorldMap(int id) {
		return worldMaps.get(id);
	}

	/**
	 * Update position of VisibleObject [used when object is moving on one map instance]. Check if active map region changed and do all needed updates.
	 */
	public void updatePosition(VisibleObject object, float newX, float newY, float newZ, byte newHeading) {
		updatePosition(object, newX, newY, newZ, newHeading, true);
	}

	public void updatePosition(VisibleObject object, float newX, float newY, float newZ, byte newHeading, boolean updateKnownList) {
		if (!object.isSpawned()) { // despawned objects should never move
			log.warn("Can't update position of despawned object: {}", object, new Throwable());
			return;
		}

		WorldPosition position = object.getPosition();
		MapRegion oldRegion = position.getMapRegion();
		if (oldRegion == null) {
			if (object instanceof Player player) {
				if (!player.isStaff()) {
					AuditLogger.log(player, "is outside valid regions: " + position);
					// he will be sent to bind point in PlayerLeaveWorldService
					player.getClientConnection().close(SM_SYSTEM_MESSAGE.STR_KICK_CHARACTER());
				}
			} else {
				log.warn("Old MapRegion was null when trying to update position of {}", object, new Throwable());
			}
			return;
		}

		MapRegion newRegion = oldRegion.getParent().getRegion(newX, newY, newZ);
		if (newRegion == null) {
			log.warn("New MapRegion for {} doesn't exist at coordinates: Map {}, X {}, Y {}, Z {}", object, object.getWorldId(), newX, newY, newZ,
				new Throwable());
			if (object instanceof Creature creature)
				creature.getMoveController().abortMove();
			if (object instanceof Player player) {
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

		position.setXYZH(newX, newY, newZ, newHeading);

		if (newRegion != oldRegion) {
			if (object instanceof Creature creature) {
				oldRegion.revalidateZones(creature);
				newRegion.revalidateZones(creature);
			}
			oldRegion.remove(object);
			newRegion.add(object);
			position.setMapRegion(newRegion);
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

	public WorldPosition createPosition(int mapId, float x, float y, float z, byte heading, int instanceId) {
		WorldMap map = getWorldMap(mapId);
		if (map == null)
			throw new NullPointerException("Failed to create position (invalid mapId: " + mapId + ")");
		if (map.getWorldMapInstance(instanceId) == null)
			throw new NullPointerException("Failed to create position (invalid instanceId " + instanceId + " for mapId " + mapId + ")");
		MapRegion mr = map.getWorldMapInstance(instanceId).getRegion(x, y, z);
		if (mr == null)
			throw new NullPointerException(
				"Failed to create position (invalid coords: x=" + x + ", y=" + y + ", z=" + z + " for mapId " + mapId + " in instanceId " + instanceId + ")");
		return new WorldPosition(mapId, x, y, z, heading, mr);
	}

	/**
	 * Spawns the object at the current position (use setPosition). Object will be visible by others and will see other objects.
	 */
	public void spawn(VisibleObject object) throws AlreadySpawnedException {
		if (object == null)
			return;
		WorldPosition position = object.getPosition();
		if (position.isSpawned())
			throw new AlreadySpawnedException(object);

		object.getController().onBeforeSpawn();
		position.setIsSpawned(true);

		position.getMapRegion().getParent().addObject(object);
		position.getMapRegion().add(object);
		object.getController().onAfterSpawn();

		object.updateKnownlist();
	}

	/**
	 * Despawns the object with the default delete animation.
	 * 
	 * @see #despawn(VisibleObject, ObjectDeleteAnimation)
	 */
	public void despawn(VisibleObject object) {
		despawn(object, ObjectDeleteAnimation.FADE_OUT);
	}

	/**
	 * Despawns the object, object will become invisible. All other objects will be noticed that this object is no longer visible.
	 */
	public void despawn(VisibleObject object, ObjectDeleteAnimation animation) {
		WorldPosition position = object.getPosition();
		try {
			object.getController().onDespawn();
		} finally {
			MapRegion oldMapRegion = position.getMapRegion();
			position.setIsSpawned(false);
			if (oldMapRegion != null) { // can be null if an instance gets deleted?
				oldMapRegion.getParent().removeObject(object);
				oldMapRegion.remove(object);
				if (object instanceof Creature creature)
					oldMapRegion.revalidateZones(creature);
			}
			object.clearKnownlist(animation);
		}
	}

	public void updateCachedPlayerName(String oldName, Player player) {
		allPlayers.updateCachedPlayerName(oldName, player);
	}

	public Collection<Player> getAllPlayers() {
		return allPlayers.getAllPlayers();
	}

	public void forEachPlayer(Consumer<Player> consumer) {
		CollectionUtil.forEach(allPlayers, consumer);
	}

	public void forEachObject(Consumer<VisibleObject> consumer) {
		CollectionUtil.forEach(allObjects.values(), consumer);
	}

	private static class SingletonHolder {

		protected static final World instance = new World();
	}

}
