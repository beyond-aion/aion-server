package com.aionemu.gameserver.model.house;

import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.model.templates.housing.HouseType;

/**
 * @author Rolandas
 */
public class HouseBidEntry implements Cloneable {

	private int entryIndex;
	private int landId;
	private int address;
	private int buildingId;
	private HouseType houseType;
	private long bidPrice;
	private final long unk2 = 100000;
	private int bidCount;
	private int mapId;
	private int lastBiddingPlayer;
	private long lastBidTime;

	public HouseBidEntry(House house, int index, long initialBid) {
		entryIndex = index;
		landId = house.getLand().getId();
		address = house.getAddress().getId();
		mapId = house.getAddress().getMapId();
		buildingId = house.getBuilding().getId();
		houseType = house.getHouseType();
		bidPrice = initialBid;
		lastBiddingPlayer = 0;
		lastBidTime = 0;
	}

	private HouseBidEntry() {
	}

	public int getEntryIndex() {
		return entryIndex;
	}

	public void setEntryIndex(int entryIndex) {
		this.entryIndex = entryIndex;
	}

	public int getLandId() {
		return landId;
	}

	public int getAddress() {
		return address;
	}

	public int getBuildingId() {
		return buildingId;
	}

	public void setBuildingId(int buildingId) {
		this.buildingId = buildingId;
	}

	public long getBidPrice() {
		return bidPrice;
	}

	public void setBidPrice(long bidPrice) {
		this.bidPrice = bidPrice;
	}

	public int getBidCount() {
		return bidCount;
	}

	public void incrementBidCount() {
		this.bidCount++;
	}

	public final long getUnk2() {
		return unk2;
	}

	public HouseType getHouseType() {
		return houseType;
	}

	public int getMapId() {
		return mapId;
	}

	public int getLastBiddingPlayer() {
		return lastBiddingPlayer;
	}

	public void setLastBiddingPlayer(int lastBiddingPlayer) {
		this.lastBiddingPlayer = lastBiddingPlayer;
	}

	public long getRefundKinah() {
		return (long) (bidPrice * HousingConfig.BID_REFUND_PERCENT);
	}

	public long getLastBidTime() {
		return lastBidTime;
	}

	public void setLastBidTime(long lastBidTime) {
		this.lastBidTime = lastBidTime;
	}

	public Object Clone() {
		HouseBidEntry cloned = new HouseBidEntry();
		cloned.address = this.address;
		cloned.bidCount = this.bidCount;
		cloned.bidPrice = this.bidPrice;
		cloned.buildingId = this.buildingId;
		cloned.entryIndex = this.entryIndex;
		cloned.houseType = this.houseType;
		cloned.landId = this.landId;
		cloned.mapId = this.mapId;
		cloned.lastBiddingPlayer = this.lastBiddingPlayer;
		return cloned;
	}

}
