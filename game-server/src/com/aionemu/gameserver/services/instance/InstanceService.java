package com.aionemu.gameserver.services.instance;

import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.configs.main.InstanceConfig;
import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.instance.InstanceEngine;
import com.aionemu.gameserver.instance.handlers.InstanceHandler;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.team.GeneralTeam;
import com.aionemu.gameserver.model.templates.housing.BuildingType;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.AutoGroupService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.spawnengine.TemporarySpawnEngine;
import com.aionemu.gameserver.spawnengine.WalkerFormator;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.*;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author ATracer
 */
public class InstanceService {

	private static final Logger log = LoggerFactory.getLogger(InstanceService.class);

	public static WorldMapInstance getNextAvailableInstance(int worldId, int ownerId, byte difficultyId, Function<WorldMapInstance, InstanceHandler> instanceHandlerSupplier, int maxPlayers, boolean autoDestroy) {
		WorldMap map = World.getInstance().getWorldMap(worldId);

		if (!map.isInstanceType() || map.getWorldType() == WorldType.PANESTERRA && !map.getAvailableInstanceIds().isEmpty())
			throw new UnsupportedOperationException("Invalid call for next available instance  of " + worldId);

		WorldMapInstance instance;
		if (instanceHandlerSupplier == null) {
			instance = WorldMapInstanceFactory.createWorldMapInstance(map, ownerId, InstanceEngine.getInstance()::getNewInstanceHandler, maxPlayers);
			SpawnEngine.spawnInstance(worldId, instance.getInstanceId(), difficultyId, ownerId);
		}	else {
			instance = WorldMapInstanceFactory.createWorldMapInstance(map, ownerId, instanceHandlerSupplier, maxPlayers);
		}
		instance.getInstanceHandler().onInstanceCreate();

		// finally start the checker
		if (autoDestroy)
			instance.setEmptyInstanceTask(ThreadPoolManager.getInstance().scheduleAtFixedRate(new EmptyInstanceCheckerTask(instance), 60000, 60000));

		log.info("Created new instance: " + worldId + " [" + instance.getInstanceId() + "] owner:" + ownerId + " difficultyId:" + difficultyId);
		return instance;
	}

	public static WorldMapInstance getNextAvailableInstance(int worldId, int ownerId, byte difficult, int maxPlayers, boolean autoDestroy) {
		return getNextAvailableInstance(worldId, ownerId, difficult, null, maxPlayers, autoDestroy);
	}

	public static WorldMapInstance getNextAvailableInstance(int worldId, Player player) {
		int maxPlayers = DataManager.INSTANCE_COOLTIME_DATA.getMaxMemberCount(worldId, player.getRace());
		WorldMapInstance instance = getNextAvailableInstance(worldId, 0, (byte) 0, null, maxPlayers, true);
		instance.register(player.getObjectId());
		return instance;
	}

	public static WorldMapInstance getNextAvailableInstance(int worldId, byte difficult, int maxPlayers) {
		return getNextAvailableInstance(worldId, 0, difficult, null, maxPlayers, true);
	}

	/**
	 * Instance will be destroyed All players moved to bind location All objects - deleted
	 */
	public static void destroyInstance(WorldMapInstance instance) {
		if (instance.getEmptyInstanceTask() != null)
			instance.getEmptyInstanceTask().cancel(false);

		int worldId = instance.getMapId();
		WorldMap map = World.getInstance().getWorldMap(worldId);
		if (!map.isInstanceType())
			return;
		int instanceId = instance.getInstanceId();

		map.removeWorldMapInstance(instanceId);

		log.info("Destroying instance:" + worldId + " " + instanceId);

		TemporarySpawnEngine.onInstanceDestroy(worldId, instanceId); // first unregister all temporary spawns, then despawn mobs
		for (VisibleObject obj : instance) {
			if (obj instanceof Player player) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LEAVE_INSTANCE_FORCE(0));
				moveToExitPoint(player);
			} else {
				obj.getController().delete();
			}
		}
		instance.getInstanceHandler().onInstanceDestroy();
		WalkerFormator.onInstanceDestroy(worldId, instanceId);
	}

	public static WorldMapInstance getOrRegisterInstance(int worldId, Player player) {
		WorldMapInstance instance = getRegisteredInstance(worldId, player.getObjectId());
		if (instance == null)
			instance = getNextAvailableInstance(worldId, player);
		return instance;
	}

	public static WorldMapInstance getRegisteredInstance(int worldId, int objectId) {
		for (WorldMapInstance instance : World.getInstance().getWorldMap(worldId)) {
			if (instance.isRegistered(objectId))
				return instance;
		}
		return null;
	}

	/**
	 * @return Instance for the given house or studio.
	 */
	public static WorldMapInstance getOrCreateHouseInstance(House house) {
		WorldMapInstance instance = house.getPosition() == null ? null : house.getPosition().getWorldMapInstance();
		if (instance == null && house.getBuilding().getType() == BuildingType.PERSONAL_INS) { // studio
			instance = getOrCreatePersonalInstance(house.getAddress().getMapId(), house.getOwnerId());
		}
		if (instance == null) // should never happen since only studios are spawned on demand
			throw new NullPointerException(house + " has no instance");
		return instance;
	}

	private static WorldMapInstance getOrCreatePersonalInstance(int worldId, int ownerId) {
		if (ownerId == 0)
			return null;

		for (WorldMapInstance instance : World.getInstance().getWorldMap(worldId)) {
			if (instance.isPersonal() && instance.getOwnerId() == ownerId)
				return instance;
		}
		return getNextAvailableInstance(worldId, ownerId, (byte) 0, 0, true);
	}

	public static WorldMapInstance getBeginnerInstance(int worldId, int registeredId) {
		WorldMapInstance instance = getRegisteredInstance(worldId, registeredId);
		if (instance == null)
			return null;
		return instance.isBeginnerInstance() ? instance : null;
	}

	private static int getLastRegisteredId(Player player) {
		int lookupId;
		boolean isPersonal = WorldMapType.getWorld(player.getWorldId()).isPersonal();
		if (player.isInGroup()) {
			lookupId = player.getPlayerGroup().getTeamId();
		} else if (player.isInAlliance()) {
			lookupId = player.getPlayerAlliance().getTeamId();
			if (player.isInLeague()) {
				lookupId = player.getPlayerAlliance().getLeague().getObjectId();
			}
		} else if (isPersonal && player.getCommonData().getWorldOwnerId() != 0) {
			lookupId = player.getCommonData().getWorldOwnerId();
		} else {
			lookupId = player.getObjectId();
		}
		return lookupId;
	}

	public static void onPlayerLogin(Player player) {
		int worldId = player.getWorldId();
		int lookupId = getLastRegisteredId(player);
		WorldMapTemplate worldTemplate = DataManager.WORLD_MAPS_DATA.getTemplate(worldId);
		if (worldTemplate.isInstance()) {
			boolean isPersonal = WorldMapType.getWorld(player.getWorldId()).isPersonal();
			WorldMapInstance registeredInstance = isPersonal ? getOrCreatePersonalInstance(worldId, lookupId) : getRegisteredInstance(worldId, lookupId);

			if (registeredInstance != null) {
				if (registeredInstance.isFull()) {
					moveToExitPoint(player);
					return;
				}
				World.getInstance().setPosition(player, worldId, registeredInstance.getInstanceId(), player.getX(), player.getY(), player.getZ(),
					player.getHeading());
				registeredInstance.getInstanceHandler().onPlayerLogin(player);
				return;
			}

			moveToExitPoint(player);
		} else {
			WorldMapInstance beginnerInstance = getBeginnerInstance(worldId, lookupId);
			if (beginnerInstance != null) {
				// set to correct twin instanceId, not to #1
				World.getInstance().setPosition(player, worldId, beginnerInstance.getInstanceId(), player.getX(), player.getY(), player.getZ(),
					player.getHeading());
			}
		}
	}

	public static void moveToExitPoint(Player player) {
		TeleportService.moveToInstanceExit(player, player.getWorldId(), player.getRace());
	}

	public static boolean instanceExists(int worldId, int instanceId) {
		return World.getInstance().getWorldMap(worldId).getWorldMapInstance(instanceId) != null;
	}

	private static class EmptyInstanceCheckerTask implements Runnable {

		private final WorldMapInstance worldMapInstance;
		private final long taskStartTime;

		private EmptyInstanceCheckerTask(WorldMapInstance worldMapInstance) {
			this.worldMapInstance = worldMapInstance;
			this.taskStartTime = System.currentTimeMillis();
		}

		private boolean canDestroyInstance() {
			if (!worldMapInstance.getPlayersInside().isEmpty())
				return false;
			return worldMapInstance.isPersonal() || isRegisteredTeamDisbanded() || System.currentTimeMillis() > calculateDestroyTime() - 1000;
		}

		private boolean isRegisteredTeamDisbanded() {
			GeneralTeam<?, ?> registeredTeam = worldMapInstance.getRegisteredTeam();
			return registeredTeam != null && registeredTeam.isDisbanded();
		}

		private long calculateDestroyTime() {
			long lastActivity = Math.max(taskStartTime, worldMapInstance.getLastPlayerLeaveTime());
			return lastActivity + getDestroyDelaySeconds(worldMapInstance) * 1000;
		}

		@Override
		public void run() {
			if (canDestroyInstance())
				destroyInstance(worldMapInstance);
		}
	}

	public static void onLogOut(Player player) {
		player.getPosition().getWorldMapInstance().getInstanceHandler().onPlayerLogOut(player);
	}

	public static void onEnterInstance(Player player) {
		player.getPosition().getWorldMapInstance().getInstanceHandler().onEnterInstance(player);
		AutoGroupService.getInstance().onEnterInstance(player);
		removeRestrictedItemsFromInventoryAndStorage(player,
			item -> item.getItemTemplate().hasWorldRestrictions() && !item.getItemTemplate().isItemRestrictedToWorld(player.getWorldId()));
	}

	public static void onLeaveInstance(Player player) {
		WorldMapInstance registeredInstance = getRegisteredInstance(player.getWorldId(), getLastRegisteredId(player));
		if (registeredInstance != null) { // don't get instance via player.getPosition since he maybe isn't registered with it anymore (login after dc)
			registeredInstance.getInstanceHandler().onLeaveInstance(player);
			if (!registeredInstance.isPersonal()) {
				if (registeredInstance.getMaxPlayers() == 1) // solo instance
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LEAVE_INSTANCE(getDestroyDelaySeconds(registeredInstance) / 60));
				else if (registeredInstance.getRegisteredTeam() != null && registeredInstance.getRegisteredTeam().getMembers().isEmpty())
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LEAVE_INSTANCE_PARTY(0));
				else if (registeredInstance.getPlayersInside().size() <= 1)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LEAVE_INSTANCE_PARTY(getDestroyDelaySeconds(registeredInstance) / 60));
			}
		}
		removeRestrictedItemsFromInventoryAndStorage(player, item -> item.getItemTemplate().isItemRestrictedToWorld(player.getWorldId()));

		if (AutoGroupConfig.AUTO_GROUP_ENABLE)
			AutoGroupService.getInstance().onLeaveInstance(player);
	}

	private static void removeRestrictedItemsFromInventoryAndStorage(Player player, Predicate<Item> conditionForItemRemoval) {
		if (conditionForItemRemoval == null)
			return;
		for (Item item : player.getInventory().getItems())
			if (conditionForItemRemoval.test(item))
				player.getInventory().decreaseByObjectId(item.getObjectId(), item.getItemCount());
		for (Storage storage : player.getPetBag()) {
			if (storage == null)
				continue;
			for (Item item : storage.getItems())
				if (conditionForItemRemoval.test(item))
					storage.decreaseByObjectId(item.getObjectId(), item.getItemCount());
		}
	}

	public static void onEnterZone(Player player, ZoneInstance zone) {
		player.getPosition().getWorldMapInstance().getInstanceHandler().onEnterZone(player, zone);
	}

	public static void onLeaveZone(Player player, ZoneInstance zone) {
		player.getPosition().getWorldMapInstance().getInstanceHandler().onLeaveZone(player, zone);
	}

	public static int getInstanceRate(Player player, int mapId) {
		return player.hasPermission(MembershipConfig.INSTANCES_COOLDOWN) && !InstanceConfig.INSTANCE_COOLDOWN_RATE_EXCLUDED_MAPS.contains(mapId) ? InstanceConfig.INSTANCE_COOLDOWN_RATE : 1;
	}

	public static int getDestroyDelaySeconds(WorldMapInstance worldMapInstance) {
		return worldMapInstance.getMaxPlayers() == 1 ? InstanceConfig.SOLO_INSTANCE_DESTROY_DELAY_SECONDS : InstanceConfig.INSTANCE_DESTROY_DELAY_SECONDS;
	}
}
