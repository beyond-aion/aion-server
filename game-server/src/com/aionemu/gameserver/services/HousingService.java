package com.aionemu.gameserver.services;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.dao.HousesDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Letter;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
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
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Rolandas, Neon
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
		customHouses = new ConcurrentHashMap<>(HousesDAO.loadHouses(DataManager.HOUSE_DATA.getLands(), false));
		studios = new ConcurrentHashMap<>(HousesDAO.loadHouses(DataManager.HOUSE_DATA.getLands(), true));
		updateInactiveStateForAllHouses();
		revokeOwnershipOfDeletedPlayers();
		log.info("Loaded " + customHouses.size() + " houses and " + studios.size() + " studios");
	}

	private void revokeOwnershipOfDeletedPlayers() {
		Set<Integer> playerIds = IntStream.of(PlayerDAO.getUsedIDs()).boxed().collect(Collectors.toSet());
		Stream.concat(customHouses.values().stream(), studios.values().stream()).forEach(house -> {
			// houses table has no player_id foreign key because houses need to stay in DB even on player deletion (to keep bidding possible for example)
			if (house.getOwnerId() > 0 && !playerIds.contains(house.getOwnerId())) {
				log.warn("Player with ID " + house.getOwnerId() + " got deleted from DB, revoking house ownership for house " + house.getAddress().getId());
				changeOwner(house, 0);
			}
		});
	}

	private void updateInactiveStateForAllHouses() {
		customHouses.values().stream().mapToInt(House::getOwnerId).distinct().forEach(this::updateInactiveStateForPlayerHouses);
	}

	/**
	 * Only for server start. Doesn't send packets or reloads house registries because it should only be run once on init.
	 */
	private void updateInactiveStateForPlayerHouses(int playerObjId) {
		Stream<House> houses = customHouses.values().stream().filter(house -> house.getOwnerId() == playerObjId);
		if (playerObjId == 0) { // not occupied houses
			houses.forEach(house -> house.setInactive(false));
		} else {
			List<House> housesSortedByAcquireDate = houses.sorted(Comparator.comparing(House::getAcquiredTime)).collect(Collectors.toList());
			for (int i = 0; i < housesSortedByAcquireDate.size(); i++)
				housesSortedByAcquireDate.get(i).setInactive(i != 0); // first house (oldest) should be active, rest inactive
		}
	}

	public void changeOwner(House house, int newOwnerId) {
		int oldOwnerId = house.getOwnerId();
		if (oldOwnerId == newOwnerId)
			return;

		synchronized (house) {
			boolean newOwnerHasAnotherHouse = newOwnerId != 0 && customHouses.values().stream().anyMatch(h -> h.getOwnerId() == newOwnerId);
			house.resetRegistry();
			house.getPlayerScripts().removeAll();
			house.setOwnerId(newOwnerId);
			if (newOwnerId == 0 && removeStudio(house)) {
				HousesDAO.deleteHouse(oldOwnerId);
				notifyAboutOwnerChange(oldOwnerId, house.getAddress().getId(), false);
				return;
			}
			house.setInactive(newOwnerHasAnotherHouse);
			house.resetDoorState();
			house.setShowOwnerName(true);
			house.setSignNotice(null);
			house.setAcquiredTime(newOwnerId == 0 ? null : new Timestamp(System.currentTimeMillis()));
			house.setNextPay(null);

			Building defaultBuilding = house.getLand().getDefaultBuilding();
			if (defaultBuilding != house.getBuilding())
				switchHouseBuilding(house, defaultBuilding.getId());
			else // in else clause because building switch also saves the house
				house.save();
		}
		House newHouseOfOldOwner = findInactiveHouse(oldOwnerId); // other house of seller that should get activated
		if (newHouseOfOldOwner != null && newHouseOfOldOwner.getPosition() != null && newHouseOfOldOwner.isSpawned()) {
			newHouseOfOldOwner.setInactive(false);
			newHouseOfOldOwner.reloadHouseRegistry();
			newHouseOfOldOwner.getController().updateSign();
			newHouseOfOldOwner.getController().updateAppearance();
		}
		notifyAboutOwnerChange(oldOwnerId, house.getAddress().getId(), false);
		notifyAboutOwnerChange(newOwnerId, house.getAddress().getId(), true);
		if (house.getPosition() != null && house.isSpawned()) {
			house.getController().updateHouseSpawns();
			house.getController().kickVisitors(null, true, true);
		}
	}

	private void notifyAboutOwnerChange(int ownerId, int addressId, boolean isNewOwner) {
		if (ownerId == 0)
			return;
		Player player = World.getInstance().getPlayer(ownerId);
		if (player != null) {
			player.resetHouses();
			if (!isNewOwner)
				PacketSendUtility.sendPacket(player, new SM_HOUSE_ACQUIRE(player.getObjectId(), addressId, false));
			PacketSendUtility.sendPacket(player, new SM_HOUSE_OWNER_INFO(player));
			if (isNewOwner)
				PacketSendUtility.sendPacket(player, new SM_HOUSE_ACQUIRE(player.getObjectId(), addressId, true));
		}
	}

	public void spawnHouses(WorldMapInstance instance, int registeredId) {
		if (registeredId > 0) {
			spawnStudio(instance.getMapId(), instance.getInstanceId(), registeredId);
			return;
		}
		int spawnedCounter = 0;
		for (HouseAddress address : DataManager.HOUSE_DATA.getAddresses(instance.getMapId())) {
			if (address.getLand().getDefaultBuilding().getType() == BuildingType.PERSONAL_INS)
				continue; // ignore studios

			House customHouse = customHouses.get(address.getId());
			if (customHouse == null) {
				customHouse = new House(address, instance.getInstanceId());
				// house without owner when acquired will be inserted to DB
				customHouse.setPersistentState(PersistentState.NEW);
				customHouses.put(address.getId(), customHouse);
			}
			WorldPosition position = World.getInstance().createPosition(address.getMapId(), address.getX(), address.getY(), address.getZ(), (byte) 0,
				instance.getInstanceId());
			customHouse.setPosition(position);
			SpawnEngine.bringIntoWorld(customHouse);
			spawnedCounter++;
		}
		if (spawnedCounter > 0) {
			log.info("Spawned " + spawnedCounter + " houses in " + instance);
		}
	}

	private void spawnStudio(int worldId, int instanceId, int registeredId) {
		House studio = getPlayerStudio(registeredId);
		if (studio == null || studio.getAddress().getMapId() != worldId)
			return;
		if (studio.getPosition() == null || studio.getInstanceId() != instanceId) {
			HouseAddress addr = studio.getAddress();
			studio.setPosition(World.getInstance().createPosition(addr.getMapId(), addr.getX(), addr.getY(), addr.getZ(), (byte) 0, instanceId));
			SpawnEngine.bringIntoWorld(studio);
		}
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
			if (house.getOwnerId() == playerObjId && !house.isInactive())
				return house;
		}
		return null;
	}

	/**
	 * @return The players current inactive house.
	 */
	public House findInactiveHouse(int playerObjId) {
		for (House house : customHouses.values()) {
			if (house.getOwnerId() == playerObjId && house.isInactive())
				return house;
		}
		return null;
	}

	public House getHouseByAddress(int address) {
		return customHouses.get(address);
	}

	public House getPlayerStudio(int playerId) {
		return studios.get(playerId);
	}

	public boolean removeStudio(House studio) {
		return studios.values().remove(studio);
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
		studios.put(player.getObjectId(), studio);
		studio.setPersistentState(PersistentState.NEW);
		changeOwner(studio, player.getObjectId());

		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_INS_OWN_SUCCESS());
	}

	public boolean canOwnHouse(Player player, boolean notify) {
		int questId = player.getRace() == Race.ELYOS ? 18802 : 28802;
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.COMPLETE) {
			if (notify)
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_OWN_NOT_COMPLETE_QUEST(questId));
			return false;
		}
		return true;
	}

	public void switchHouseBuilding(House currentHouse, int newBuildingId) {
		currentHouse.setBuilding(DataManager.HOUSE_BUILDING_DATA.getBuilding(newBuildingId));
		currentHouse.save();
		currentHouse.reloadHouseRegistry(); // load new defaults
		currentHouse.getController().spawnObjects();
	}

	public List<House> getCustomHouses() {
		return new ArrayList<>(customHouses.values());
	}

	public House findHouse(int objId) {
		for (House house : customHouses.values()) {
			if (house.getObjectId() == objId)
				return house;
		}
		return null;
	}

	public House findStudio(int objId) {
		for (House studio : studios.values()) {
			if (studio.getObjectId() == objId)
				return studio;
		}
		return null;
	}

	public House findHouseOrStudio(int objId) {
		House studio = findStudio(objId);
		return studio == null ? findHouse(objId) : studio;
	}

	public void onPlayerDeleted(int playerObjId) {
		HousingBidService.getInstance().disableBids(playerObjId);
		findPlayerHouses(playerObjId).forEach(house -> changeOwner(house, 0));
	}

	public void onPlayerLogin(Player player) {
		House activeHouse = player.getActiveHouse();
		if (activeHouse != null) {
			if (HousingConfig.ENABLE_HOUSE_PAY && activeHouse.getNextPay() != null && activeHouse.getNextPay().getTime() <= System.currentTimeMillis())
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OVERDUE());
		} else {
			for (Letter letter : player.getMailbox().getNewSystemLetters("$$HS_OVERDUE_")) {
				if (letter.getSenderName().endsWith("FINAL") || letter.getSenderName().endsWith("3RD")) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_SEQUESTRATE());
					break;
				}
			}
		}
		PacketSendUtility.sendPacket(player, new SM_HOUSE_OWNER_INFO(player));
	}
}
