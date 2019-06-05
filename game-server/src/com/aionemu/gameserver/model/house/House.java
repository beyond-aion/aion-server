package com.aionemu.gameserver.model.house;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.controllers.HouseController;
import com.aionemu.gameserver.dao.HouseScriptsDAO;
import com.aionemu.gameserver.dao.HousesDAO;
import com.aionemu.gameserver.dao.PlayerRegisteredItemsDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.HouseDecoration;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.HouseOwnerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerScripts;
import com.aionemu.gameserver.model.templates.housing.Building;
import com.aionemu.gameserver.model.templates.housing.BuildingType;
import com.aionemu.gameserver.model.templates.housing.HouseAddress;
import com.aionemu.gameserver.model.templates.housing.HouseType;
import com.aionemu.gameserver.model.templates.housing.HousingLand;
import com.aionemu.gameserver.model.templates.housing.PartType;
import com.aionemu.gameserver.model.templates.housing.Sale;
import com.aionemu.gameserver.model.templates.spawns.HouseSpawn;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnType;
import com.aionemu.gameserver.services.HousingBidService;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.knownlist.PlayerAwareKnownList;

/**
 * @author Rolandas
 */
public class House extends VisibleObject implements Persistable {

	private static final Logger log = LoggerFactory.getLogger(House.class);
	private final HouseAddress address;
	private Building building;
	private int ownerId;
	private Timestamp acquiredTime;
	private int permissions;
	private HouseStatus status;
	private Timestamp nextPay;
	private Timestamp sellStarted;
	private Map<SpawnType, Npc> spawns = new EnumMap<>(SpawnType.class);
	private HouseRegistry houseRegistry;
	private byte houseOwnerStates = HouseOwnerState.SINGLE_HOUSE.getId();
	private PlayerScripts playerScripts;
	private PersistentState persistentState;
	private String signNotice;

	public House(HouseAddress address, int instanceId) {
		this(IDFactory.getInstance().nextId(), address.getLand().getDefaultBuilding(), address, instanceId);
	}

	public House(int objectId, Building building, HouseAddress address, int instanceId) {
		super(objectId, new HouseController(), null, null, null);
		getController().setOwner(this);
		this.address = address;
		this.building = building;
		setKnownlist(new PlayerAwareKnownList(this));
		setPersistentState(PersistentState.UPDATED);
		getRegistry();
	}

	@Override
	public HouseController getController() {
		return (HouseController) super.getController();
	}

	public HousingLand getLand() {
		return address.getLand();
	}

	@Override
	public String getName() {
		return "HOUSE_" + address.getId();
	}

	public HouseAddress getAddress() {
		return address;
	}

	public Building getBuilding() {
		return building;
	}

	public void setBuilding(Building building) {
		this.building = building;
	}

	public synchronized void spawn(int instanceId) {
		playerScripts = DAOManager.getDAO(HouseScriptsDAO.class).getPlayerScripts(getObjectId());

		if (ownerId > 0 && (status == HouseStatus.ACTIVE || status == HouseStatus.SELL_WAIT))
			reloadHouseRegistry();

		fixBuildingStates();

		// Studios are brought into world already, skip them
		if (getPosition() == null || !getPosition().isSpawned()) {
			WorldPosition position = World.getInstance().createPosition(address.getMapId(), address.getX(), address.getY(), address.getZ(), (byte) 0,
				instanceId);
			this.setPosition(position);
			SpawnEngine.bringIntoWorld(this);
		}

		spawnNpcs();
	}

	private void spawnNpcs() {
		String masterName = ownerId == 0 ? null : PlayerService.getPlayerName(ownerId);
		List<HouseSpawn> templates = DataManager.HOUSE_NPCS_DATA.getSpawnsByAddress(getAddress().getId());
		if (templates == null) {
			log.warn("Missing npc spawns for house " + getAddress().getId());
			return;
		}
		for (HouseSpawn spawn : templates) {
			Npc npc;
			if (spawn.getType() == SpawnType.MANAGER) {
				SpawnTemplate t = SpawnEngine.newSingleTimeSpawn(getAddress().getMapId(), getLand().getManagerNpcId(), spawn.getX(), spawn.getY(),
						spawn.getZ(), spawn.getH());
				npc = VisibleObjectSpawner.spawnHouseNpc(t, getInstanceId(), this, masterName);
			} else if (spawn.getType() == SpawnType.TELEPORT) {
				SpawnTemplate t = SpawnEngine.newSingleTimeSpawn(getAddress().getMapId(), getLand().getTeleportNpcId(), spawn.getX(), spawn.getY(),
						spawn.getZ(), spawn.getH());
				npc = VisibleObjectSpawner.spawnHouseNpc(t, getInstanceId(), this, masterName);
			} else if (spawn.getType() == SpawnType.SIGN) {
				// Signs do not have master name displayed, but have creatorId
				int creatorId = getAddress().getId();
				SpawnTemplate t = SpawnEngine.newSingleTimeSpawn(getAddress().getMapId(), getCurrentSignNpcId(), spawn.getX(), spawn.getY(), spawn.getZ(),
						spawn.getH(), creatorId);
				npc = (Npc) SpawnEngine.spawnObject(t, getInstanceId());
			} else {
				log.warn("Unhandled spawn type " + spawn.getType());
				continue;
			}
			if (npc == null)
				log.warn("Invalid " + spawn.getType() + " npc ID for house " + getAddress());
			else if (spawns.putIfAbsent(spawn.getType(), npc) != null)
				log.warn("Duplicate " + spawn.getType() + " spawn for house " + getAddress());
		}
	}

	@Override
	public float getVisibleDistance() {
		return HousingConfig.VISIBILITY_DISTANCE;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		if (this.ownerId != ownerId) {
			int oldOwnerId = this.ownerId;
			this.ownerId = ownerId;
			signNotice = null;
			houseRegistry = null;
			resetCachedHousesOfPlayer(oldOwnerId);
			resetCachedHousesOfPlayer(ownerId);
		}
		fixBuildingStates();
	}

	private void resetCachedHousesOfPlayer(int ownerId) {
		if (ownerId > 0) {
			Player player = World.getInstance().findPlayer(ownerId);
			if (player != null)
				player.resetHouses();
		}
	}

	public Timestamp getAcquiredTime() {
		return acquiredTime;
	}

	public void setAcquiredTime(Timestamp acquiredTime) {
		this.acquiredTime = acquiredTime;
	}

	public int getPermissions() {
		if (ownerId == 0) {
			setDoorState(status == HouseStatus.SELL_WAIT ? HousePermissions.DOOR_OPENED_ALL : HousePermissions.DOOR_CLOSED);
			setNoticeState(HousePermissions.NOT_SET);
		} else {
			if (permissions == 0) {
				setNoticeState(HousePermissions.SHOW_OWNER);
				if (getBuilding().getType() == BuildingType.PERSONAL_FIELD) {
					setDoorState(HousePermissions.DOOR_CLOSED);
				}
			}
		}
		return permissions;
	}

	public void setPermissions(int permissions) {
		this.permissions = permissions;
	}

	public HousePermissions getDoorState() {
		return HousePermissions.getDoorState(getPermissions());
	}

	public void setDoorState(HousePermissions doorState) {
		permissions = HousePermissions.setDoorState(permissions, doorState);
	}

	public HousePermissions getNoticeState() {
		return HousePermissions.getNoticeState(getPermissions());
	}

	public void setNoticeState(HousePermissions noticeState) {
		permissions = HousePermissions.setNoticeState(permissions, noticeState);
	}

	public synchronized HouseStatus getStatus() {
		return status;
	}

	public synchronized void setStatus(HouseStatus status) {
		if (this.status != status) {
			// fix invalid status from DB, or automatically remove sign from not auctioned houses
			if (this.ownerId == 0 && status == HouseStatus.ACTIVE) {
				status = HouseStatus.NOSALE;
			}
			this.status = status;
			fixBuildingStates();

			if ((status != HouseStatus.INACTIVE || getSellStarted() != null) && spawns.get(SpawnType.SIGN) != null) {
				Npc sign = spawns.get(SpawnType.SIGN);
				int oldNpcId = sign.getNpcId();
				int newNpcId = getCurrentSignNpcId();

				if (newNpcId != oldNpcId) {
					SpawnTemplate t = sign.getSpawn();
					sign.getController().delete();
					t = SpawnEngine.newSingleTimeSpawn(t.getWorldId(), newNpcId, t.getX(), t.getY(), t.getZ(), t.getHeading());
					sign = (Npc) SpawnEngine.spawnObject(t, getInstanceId());
					spawns.put(SpawnType.SIGN, sign);
				}
			}
		}
	}

	public boolean isFeePaid() {
		return nextPay == null || nextPay.getTime() >= System.currentTimeMillis();
	}

	public Timestamp getNextPay() {
		return nextPay;
	}

	public void setNextPay(Date nextPay) {
		Timestamp result = null;
		if (nextPay != null) { // round to midnight
			result = new Timestamp(DateUtils.round(nextPay, Calendar.DAY_OF_MONTH).getTime());
		}
		this.nextPay = result;
	}

	public Timestamp getSellStarted() {
		return sellStarted;
	}

	public void setSellStarted(Timestamp sellStarted) {
		this.sellStarted = sellStarted;
	}

	/**
	 * @return True if this house is currently still owned by a player who just bought a new house. The new house is inactive until the grace period
	 *         ends (when this house is sold).
	 */
	public boolean isInGracePeriod() {
		return ownerId > 0 && status != HouseStatus.INACTIVE && HousingService.getInstance().findPlayerHouses(ownerId).size() > 1;
	}

	public synchronized Npc getButler() {
		return spawns.get(SpawnType.MANAGER);
	}

	public Race getPlayerRace() {
		if (getButler() == null)
			return Race.NONE;
		if (getButler().getTribe() == TribeClass.GENERAL)
			return Race.ELYOS;
		return Race.ASMODIANS;
	}

	public synchronized Npc getRelationshipCrystal() {
		return spawns.get(SpawnType.TELEPORT);
	}

	public synchronized Npc getCurrentSign() {
		return spawns.get(SpawnType.SIGN);
	}

	/**
	 * Do not use directly !!! It's for instance destroy of studios only (studios and their instance get reused, but house npcs are respawned)
	 */
	public synchronized void despawnNpcs() {
		for (Npc npc : spawns.values()) {
			npc.getController().delete();
		}
		spawns.clear();
	}

	public int getCurrentSignNpcId() {
		int npcId = getLand().getWaitingSignNpcId(); // bidding closed
		if (status == HouseStatus.NOSALE)
			npcId = getLand().getNosaleSignNpcId(); // invisible npc
		else if (status == HouseStatus.SELL_WAIT) {
			if (HousingBidService.getInstance().isBiddingAllowed())
				npcId = getLand().getSaleSignNpcId(); // bidding open
		} else if (ownerId != 0 && status == HouseStatus.ACTIVE) {
			npcId = getLand().getHomeSignNpcId(); // resident information
		}
		return npcId;
	}

	public synchronized boolean revokeOwner() {
		if (ownerId == 0)
			return false;
		getRegistry().despawnObjects();
		despawnNpcs();
		setOwnerId(0);
		if (playerScripts == null)
			playerScripts = DAOManager.getDAO(HouseScriptsDAO.class).getPlayerScripts(getObjectId());
		playerScripts.removeAll();
		if (getBuilding().getType() == BuildingType.PERSONAL_INS) {
			HousingService.getInstance().removeStudio(ownerId);
			DAOManager.getDAO(HousesDAO.class).deleteHouse(ownerId);
			return true;
		}
		acquiredTime = null;
		sellStarted = null;
		nextPay = null;

		Building defaultBuilding = getLand().getDefaultBuilding();
		if (defaultBuilding != building)
			HousingService.getInstance().switchHouseBuilding(this, defaultBuilding.getId());
		if (getStatus() != HouseStatus.SELL_WAIT)
			setStatus(HouseStatus.NOSALE);
		save();
		if (getPosition() != null && isSpawned())
			spawnNpcs();
		return true;
	}

	public HouseRegistry getRegistry() {
		if (houseRegistry == null)
			reloadHouseRegistry();
		return houseRegistry;
	}

	public synchronized void reloadHouseRegistry() {
		houseRegistry = new HouseRegistry(this);
		if (ownerId != 0)
			DAOManager.getDAO(PlayerRegisteredItemsDAO.class).loadRegistry(houseRegistry);
	}

	public HouseDecoration getRenderPart(PartType partType, int room) {
		return getRegistry().getRenderPart(partType, room);
	}

	public HouseDecoration getDefaultPart(PartType partType, int room) {
		return getRegistry().getDefaultPartByType(partType, room);
	}

	public PlayerScripts getPlayerScripts() {
		return playerScripts;
	}

	public HouseType getHouseType() {
		return HouseType.fromValue(getBuilding().getSize());
	}

	public synchronized void save() {
		DAOManager.getDAO(HousesDAO.class).storeHouse(this);
		// save registry if needed
		if (houseRegistry != null)
			this.houseRegistry.save();
	}

	@Override
	public PersistentState getPersistentState() {
		return persistentState;
	}

	@Override
	public void setPersistentState(PersistentState persistentState) {
		this.persistentState = persistentState;
	}

	public byte getHouseOwnerStates() {
		return houseOwnerStates;
	}

	public void fixBuildingStates() {
		houseOwnerStates = HouseOwnerState.SINGLE_HOUSE.getId();
		if (ownerId != 0) {
			houseOwnerStates |= HouseOwnerState.HAS_OWNER.getId();
			if (status == HouseStatus.ACTIVE) {
				houseOwnerStates |= HouseOwnerState.BIDDING_ALLOWED.getId();
				houseOwnerStates &= ~HouseOwnerState.SINGLE_HOUSE.getId();
			}
		} else if (status == HouseStatus.SELL_WAIT) {
			houseOwnerStates = HouseOwnerState.SELLING_HOUSE.getId();
		}
	}

	public String getSignNotice() {
		return signNotice;
	}

	public void setSignNotice(String notice) {
		signNotice = notice;
	}

	public boolean canEnter(Player player) {
		if (getOwnerId() != player.getObjectId() && !player.hasAccess(AdminConfig.HOUSE_ENTER_ALL)) {
			switch (getDoorState()) {
				case DOOR_CLOSED:
					return false;
				case DOOR_OPENED_FRIENDS:
					if (player.getFriendList().getFriend(getOwnerId()) == null && (player.getLegion() == null || !player.getLegion().isMember(getOwnerId())))
						return false;
			}
		}
		return true;
	}

	public final long getDefaultAuctionPrice() {
		Sale saleOptions = getLand().getSaleOptions();
		switch (getHouseType()) {
			case HOUSE:
				if (HousingConfig.HOUSE_MIN_BID > 0)
					return HousingConfig.HOUSE_MIN_BID;
				break;
			case MANSION:
				if (HousingConfig.MANSION_MIN_BID > 0)
					return HousingConfig.MANSION_MIN_BID;
				break;
			case ESTATE:
				if (HousingConfig.ESTATE_MIN_BID > 0)
					return HousingConfig.ESTATE_MIN_BID;
				break;
			case PALACE:
				if (HousingConfig.PALACE_MIN_BID > 0)
					return HousingConfig.PALACE_MIN_BID;
				break;
		}
		return saleOptions.getGoldPrice();
	}

	/**
	 * @return Calculated heading for a player inside looking towards the wall where butler, relationship crystal and the door are located.
	 */
	public byte getTeleportHeading() {
		return PositionUtil.getHeadingTowards(getX(), getY(), getRelationshipCrystal().getSpawn().getX(), getRelationshipCrystal().getSpawn().getY());
	}
}
