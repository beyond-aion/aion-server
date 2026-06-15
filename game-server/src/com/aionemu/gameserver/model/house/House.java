package com.aionemu.gameserver.model.house;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateUtils;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.controllers.HouseController;
import com.aionemu.gameserver.dao.HouseScriptsDAO;
import com.aionemu.gameserver.dao.HousesDAO;
import com.aionemu.gameserver.dao.PlayerRegisteredItemsDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.HouseOwnerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerScripts;
import com.aionemu.gameserver.model.templates.housing.*;
import com.aionemu.gameserver.model.templates.spawns.SpawnType;
import com.aionemu.gameserver.services.TownService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.taskmanager.tasks.housing.AuctionEndTask;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldType;
import com.aionemu.gameserver.world.geo.GeoService;
import com.aionemu.gameserver.world.knownlist.PlayerAwareKnownList;

/**
 * @author Rolandas
 */
public class House extends VisibleObject implements Persistable {

	private final HouseAddress address;
	private Building building;
	private int ownerId;
	private Timestamp acquiredTime;
	private HouseDoorState doorState;
	private boolean showOwnerName = true;
	private boolean inactive;
	private Timestamp nextPay;
	private HouseBids bids;
	private final Map<SpawnType, Npc> spawns = new EnumMap<>(SpawnType.class);
	private HouseRegistry houseRegistry;
	private PlayerScripts playerScripts;
	private PersistentState persistentState;
	private String signNotice;

	public House(HouseAddress address, int instanceId) {
		this(IDFactory.getInstance().nextId(), address.getLand().getDefaultBuilding(), address, instanceId);
	}

	public House(int objectId, Building building, HouseAddress address, int instanceId) {
		super(objectId, new HouseController(), null, null, null, false);
		getController().setOwner(this);
		this.address = address;
		this.building = building;
		setKnownlist(new PlayerAwareKnownList(this));
		resetDoorState();
		setPersistentState(PersistentState.UPDATED);
	}

	@Override
	public HouseController getController() {
		return (HouseController) super.getController();
	}

	@Override
	public String getName() {
		return "HOUSE_" + address.getId();
	}

	public HouseAddress getAddress() {
		return address;
	}

	public HousingLand getLand() {
		return address.getLand();
	}

	@Override
	public WorldType getWorldType() {
		return World.getInstance().getWorldMap(getAddress().getMapId()).getWorldType();
	}

	public Building getBuilding() {
		return building;
	}

	public void setBuilding(Building building) {
		this.building = building;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	@Override
	public float getVisibleDistance() {
		return HousingConfig.VISIBILITY_DISTANCE;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public String getOwnerName() {
		if (ownerId == 0)
			return null;
		Npc butler = getButler();
		return butler == null ? PlayerService.getPlayerName(ownerId) : butler.getMasterName();
	}

	public Timestamp getAcquiredTime() {
		return acquiredTime;
	}

	public void setAcquiredTime(Timestamp acquiredTime) {
		this.acquiredTime = acquiredTime;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public int getPermissionsForDB() {
		int permissions = showOwnerName ? 1 : 0;
		permissions |= doorState.getId() << 8;
		return permissions;
	}

	public void setPermissionsFromDB(int permissions) {
		showOwnerName = (permissions & 0xFF) == 1;
		doorState = HouseDoorState.get((byte) (permissions >> 8));
		if (doorState == null)
			resetDoorState();
	}

	public HouseDoorState getDoorState() {
		return doorState;
	}

	public boolean resetDoorState() {
		return setDoorState(inactive || ownerId == 0 && bids == null ? HouseDoorState.CLOSED : HouseDoorState.OPEN);
	}

	public boolean setDoorState(HouseDoorState doorState) {
		if (this.doorState == doorState)
			return false;
		this.doorState = doorState;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
		if (getPosition() != null && isSpawned())
			GeoService.getInstance().setHouseDoorState(address.getMapId(), getInstanceId(), address.getId(), getDoorState());
		return true;
	}

	/**
	 * @return True if the owner name should be displayed in the house sign tooltip
	 */
	public boolean isShowOwnerName() {
		return showOwnerName;
	}

	public void setShowOwnerName(boolean showOwnerName) {
		this.showOwnerName = showOwnerName;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * @return True if the owner of this house has another (newly acquired) house
	 */
	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
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
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public HouseBids getBids() {
		return bids;
	}

	public void setBids(HouseBids bids, boolean resetDoorStateOfUnoccupiedHouse) {
		this.bids = bids;
		if (resetDoorStateOfUnoccupiedHouse && ownerId == 0 && resetDoorState())
			save();
	}

	public Npc getButler() {
		return getSpawn(SpawnType.MANAGER);
	}

	public Npc getRelationshipCrystal() {
		return getSpawn(SpawnType.TELEPORT);
	}

	public Npc getCurrentSign() {
		return getSpawn(SpawnType.SIGN);
	}

	private Npc getSpawn(SpawnType type) {
		synchronized (spawns) {
			return spawns.get(type);
		}
	}

	public void updateSpawn(SpawnType type, Npc npc) {
		Npc oldSpawn;
		synchronized (spawns) {
			oldSpawn = spawns.put(type, npc);
		}
		if (oldSpawn != null)
			oldSpawn.getController().delete();
	}

	/**
	 * Do not use directly !!! It's for instance destroy of studios only. Studios get reused, Npcs are despawned by instance destroy
	 */
	public void clearSpawns() {
		synchronized (spawns) {
			spawns.clear();
		}
	}

	public HouseRegistry getRegistry() {
		if (houseRegistry == null)
			reloadHouseRegistry();
		return houseRegistry;
	}

	public synchronized void resetRegistry() {
		getRegistry().reset();
		houseRegistry = null;
	}

	public synchronized void reloadHouseRegistry() {
		houseRegistry = new HouseRegistry(this);
		if (ownerId != 0 && !isInactive())
			PlayerRegisteredItemsDAO.loadRegistry(houseRegistry);
	}

	public PlayerScripts getPlayerScripts() {
		if (playerScripts == null)
			reloadPlayerScripts();
		return playerScripts;
	}

	public synchronized void reloadPlayerScripts() {
		playerScripts = HouseScriptsDAO.getPlayerScripts(getObjectId());
	}

	public HouseType getHouseType() {
		return getBuilding().getSize();
	}

	public synchronized void save() {
		HousesDAO.storeHouse(this);
		if (houseRegistry != null)
			houseRegistry.save();
	}

	@Override
	public PersistentState getPersistentState() {
		return persistentState;
	}

	@Override
	@SuppressWarnings("fallthrough")
	public void setPersistentState(PersistentState persistentState) {
		switch (persistentState) {
			case DELETED:
				if (this.persistentState == PersistentState.NEW)
					this.persistentState = PersistentState.NOACTION;
				else
					this.persistentState = PersistentState.DELETED;
				break;
			case UPDATE_REQUIRED:
				if (this.persistentState == PersistentState.NEW)
					break;
			default:
				this.persistentState = persistentState;
		}
	}

	public byte getHouseOwnerStates() {
		// logic and enum names surely aren't right, but it's what allegedly got sent on retail some time in the past (?):
		// 1|2 or 2 without owner, 1|2 or 1|4 with owner - we can make it right once we know what these values control
		if (isInactive()) // only houses with owner can be inactive
			return HouseOwnerState.BIDDING_ALLOWED.getId();
		else if (ownerId == 0 && getBids() == null)
			return HouseOwnerState.SINGLE_HOUSE.getId();
		else
			return (byte) (HouseOwnerState.HAS_OWNER.getId() | HouseOwnerState.BIDDING_ALLOWED.getId());
	}

	public String getSignNotice() {
		return signNotice;
	}

	public void setSignNotice(String notice) {
		signNotice = notice;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public boolean canEnter(Player player) {
		if ((getOwnerId() != player.getObjectId() || isInactive()) && !player.hasAccess(AdminConfig.HOUSE_ENTER_ALL)) {
			switch (getDoorState()) {
				case CLOSED:
					return false;
				case CLOSED_EXCEPT_FRIENDS:
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

	public int getTownLevel() {
		if (getAddress().getTownId() == 0)
			return 0;
		return TownService.getInstance().getTownById(getAddress().getTownId()).getLevel();
	}

	/**
	 * @return Seconds until this inactive house will be activated and the old one gets removed from the owner. Returns -1 if this house is already
	 *         active.
	 */
	public int secondsUntilGraceEnd() {
		if (isInactive()) {
			Date graceEndTime = findGraceEndTime();
			return Math.max(0, (int) ((graceEndTime.getTime() - System.currentTimeMillis()) / 1000));
		}
		return -1;
	}

	/**
	 * Grace end happens on auction end, so we find the nearest auction end date taking place around two weeks after the house was bought.
	 */
	private Date findGraceEndTime() {
		long maxGraceEndTimeMillis = getAcquiredTime().getTime() + TimeUnit.DAYS.toMillis(14);
		Date auctionEndTime = AuctionEndTask.getInstance().getNextRunAfter(getAcquiredTime());
		Date graceEndTime = auctionEndTime;
		while ((auctionEndTime = AuctionEndTask.getInstance().getNextRunAfter(auctionEndTime)).getTime() <= maxGraceEndTimeMillis)
			graceEndTime = auctionEndTime;
		return graceEndTime;
	}

	public boolean matchesLandRace(Race race) {
		boolean isEly = DataManager.NPC_DATA.getNpcTemplate(getLand().getManagerNpcId()).getTribe() == TribeClass.GENERAL;
		return race == Race.ELYOS && isEly || race == Race.ASMODIANS && !isEly;
	}

	public void sendScripts(Player player) {
		if (playerScripts == null)
			return;
		playerScripts.sendToPlayer(player, address.getId());
	}

}
