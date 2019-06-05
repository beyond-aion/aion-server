package com.aionemu.gameserver.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.controllers.HouseController;
import com.aionemu.gameserver.dao.HousesDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.HouseDecoration;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.HouseOwnerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.HouseStatus;
import com.aionemu.gameserver.model.templates.housing.Building;
import com.aionemu.gameserver.model.templates.housing.BuildingType;
import com.aionemu.gameserver.model.templates.housing.HouseAddress;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_ACQUIRE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_OWNER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author Rolandas
 */
public class HousingService {

	private static final Logger log = LoggerFactory.getLogger(HousingService.class);
	// Contains all houses by their addresses
	private final Map<Integer, House> customHouses;
	private final Map<Integer, House> studios;

	private static class SingletonHolder {

		protected static final HousingService instance = new HousingService();
	}

	public static HousingService getInstance() {
		return SingletonHolder.instance;
	}

	private HousingService() {
		log.info("Loading housing data...");
		customHouses = new ConcurrentHashMap<>(DAOManager.getDAO(HousesDAO.class).loadHouses(DataManager.HOUSE_DATA.getLands(), false));
		studios = new ConcurrentHashMap<>(DAOManager.getDAO(HousesDAO.class).loadHouses(DataManager.HOUSE_DATA.getLands(), true));
		Set<Integer> playerIds = IntStream.of(DAOManager.getDAO(PlayerDAO.class).getUsedIDs()).boxed().collect(Collectors.toSet());
		Stream.concat(customHouses.values().stream(), studios.values().stream()).forEach(house -> {
			// houses table has no player_id foreign key because houses need to stay in DB even on player deletion (to keep bidding possible for example)
			if (house.getOwnerId() > 0 && !playerIds.contains(house.getOwnerId())) {
				log.warn("Player with ID " + house.getOwnerId() + " got deleted from DB, revoking house ownership for house " + house.getAddress().getId());
				house.revokeOwner();
			}
		});
		log.info("Housing Service loaded.");
	}

	public void spawnHouses(int worldId, int instanceId, int registeredId) {
		if (registeredId > 0) {
			spawnStudio(worldId, instanceId, registeredId);
			return;
		}
		int spawnedCounter = 0;
		for (HouseAddress address : DataManager.HOUSE_DATA.getAddresses(worldId)) {
			if (address.getLand().getDefaultBuilding().getType() == BuildingType.PERSONAL_INS)
				continue; // ignore studios

			House customHouse = customHouses.get(address.getId());
			if (customHouse == null) {
				customHouse = new House(address, instanceId);
				// house without owner when acquired will be inserted to DB
				customHouse.setPersistentState(PersistentState.NEW);
				customHouses.put(address.getId(), customHouse);
			}
			customHouse.spawn(instanceId);
			spawnedCounter++;
		}
		if (spawnedCounter > 0) {
			log.info("Spawned houses " + worldId + " [" + instanceId + "]: " + spawnedCounter);
		}
	}

	private void spawnStudio(int worldId, int instanceId, int registeredId) {
		House studio = getPlayerStudio(registeredId);
		if (studio == null || studio.getAddress().getMapId() != worldId)
			return;
		if (studio.getPosition() == null || studio.getInstanceId() != instanceId) {
			HouseAddress addr = studio.getAddress();
			studio.setPosition(World.getInstance().createPosition(addr.getMapId(), addr.getX(), addr.getY(), addr.getZ(), (byte) 0, instanceId));
		}
		if (!studio.isSpawned())
			SpawnEngine.bringIntoWorld(studio);
		// spawn only npcs
		studio.spawn(instanceId);
	}

	public List<House> findPlayerHouses(int playerObjId) {
		if (studios.containsKey(playerObjId)) {
			return Collections.singletonList(studios.get(playerObjId));
		}
		List<House> houses = new ArrayList<>();
		for (House house : customHouses.values()) {
			if (house.getOwnerId() == playerObjId)
				houses.add(house);
		}
		return houses;
	}

	/**
	 * @return The players studio or current active house.
	 */
	public House findActiveHouse(int playerObjId) {
		if (studios.containsKey(playerObjId))
			return studios.get(playerObjId);
		for (House house : customHouses.values()) {
			if (house.getOwnerId() == playerObjId && (house.getStatus() == HouseStatus.ACTIVE || house.getStatus() == HouseStatus.SELL_WAIT))
				return house;
		}
		return null;
	}

	public void resetAppearance(House house) {
		List<HouseDecoration> customParts = house.getRegistry().getCustomParts();
		for (HouseDecoration deco : customParts) {
			deco.setPersistentState(PersistentState.DELETED);
		}
		for (HouseDecoration deco : customParts) {
			house.getRegistry().removeCustomPart(deco.getObjectId());
		}
	}

	public House getHouseByName(String houseName) {
		for (House house : customHouses.values()) {
			if (house.getName().equals(houseName))
				return house;
		}
		return null;
	}

	public House getHouseByAddress(int address) {
		return customHouses.get(address);
	}

	public House activateBoughtHouse(int playerId) {
		for (House house : customHouses.values()) {
			if (house.getOwnerId() == playerId && house.getStatus() == HouseStatus.INACTIVE) {
				house.revokeOwner();
				house.setOwnerId(playerId);
				house.setStatus(HouseStatus.ACTIVE);
				house.setNextPay(null);
				house.setSellStarted(null);
				house.save();
				return house;
			}
		}
		return null;
	}

	public House getPlayerStudio(int playerId) {
		return studios.get(playerId);
	}

	public void removeStudio(int playerId) {
		if (playerId != 0) {
			studios.remove(playerId);
		}
	}

	public void registerPlayerStudio(Player player) {
		createStudio(player, false);
	}

	public void recreatePlayerStudio(Player player) {
		createStudio(player, true);
	}

	private void createStudio(Player player, boolean chargeFee) {
		if (!player.getHouses().isEmpty()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_INS_CANT_OWN_MORE_HOUSE());
			return;
		}
		HouseAddress address = DataManager.HOUSE_DATA.getStudioAddress(player.getRace());
		if (chargeFee && !player.getInventory().tryDecreaseKinah(address.getLand().getSaleOptions().getGoldPrice())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_NOT_ENOUGH_MONEY());
			return;
		}

		House studio = new House(address, 0);
		studio.setOwnerId(player.getObjectId());

		studios.put(player.getObjectId(), studio);
		studio.setStatus(HouseStatus.ACTIVE);
		studio.setAcquiredTime(new Timestamp(System.currentTimeMillis()));
		studio.setNextPay(null);
		studio.setPersistentState(PersistentState.NEW);
		player.setHouseOwnerState(HouseOwnerState.HOUSE_OWNER.getId());
		PacketSendUtility.sendPacket(player, new SM_HOUSE_ACQUIRE(player.getObjectId(), studio.getAddress().getId(), true));
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_INS_OWN_SUCCESS());
		PacketSendUtility.sendPacket(player, new SM_HOUSE_OWNER_INFO(player));
	}

	public void switchHouseBuilding(House currentHouse, int newBuildingId) {
		Building otherBuilding = DataManager.HOUSE_BUILDING_DATA.getBuilding(newBuildingId);
		currentHouse.setBuilding(otherBuilding);
		// currentHouse.getRegistry().despawnObjects(false);
		currentHouse.getRegistry().save();
		currentHouse.reloadHouseRegistry(); // load new defaults
		DAOManager.getDAO(HousesDAO.class).storeHouse(currentHouse);
		HouseController controller = (currentHouse.getController());
		controller.broadcastAppearance();
		controller.spawnObjects();
	}

	public List<House> getCustomHouses() {
		return new ArrayList<>(customHouses.values());
	}

	public House findHouseOrStudio(int objId) {
		for (House studio : studios.values()) {
			if (studio.getObjectId() == objId)
				return studio;
		}
		for (House house : customHouses.values()) {
			if (house.getObjectId() == objId)
				return house;
		}
		return null;
	}

	public void onInstanceDestroy(int ownerId) {
		House studio = studios.get(ownerId);
		if (studio != null) {
			studio.despawnNpcs();
			studio.save();
			studio.setPosition(null); // release mapregion with destroyed worldmapinstance
		}
	}

	public void onPlayerDeleted(int playerObjId) {
		House studio = getPlayerStudio(playerObjId);
		if (studio != null)
			studio.revokeOwner();
		customHouses.values().forEach(house -> {
			if (house.getOwnerId() == playerObjId)
				house.revokeOwner();
		});
	}

	public void onPlayerLogin(Player player) {
		byte buildingState = HouseOwnerState.BUY_STUDIO_ALLOWED.getId();
		House activeHouse = player.getActiveHouse();
		if (activeHouse == null) {
			QuestState qs;
			qs = player.getQuestStateList().getQuestState(player.getRace() == Race.ELYOS ? 18802 : 28802);
			if (qs != null && qs.getStatus().equals(QuestStatus.COMPLETE)) {
				buildingState |= HouseOwnerState.BIDDING_ALLOWED.getId();
			}
		} else {
			if (activeHouse.getStatus() == HouseStatus.SELL_WAIT)
				buildingState = HouseOwnerState.SELLING_HOUSE.getId();
			else
				buildingState = HouseOwnerState.HOUSE_OWNER.getId();
			if (HousingConfig.ENABLE_HOUSE_PAY && activeHouse.getNextPay() != null && activeHouse.getNextPay().getTime() <= System.currentTimeMillis())
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OVERDUE());
		}
		player.setHouseOwnerState(buildingState);

		PacketSendUtility.sendPacket(player, new SM_HOUSE_OWNER_INFO(player));
	}
}
