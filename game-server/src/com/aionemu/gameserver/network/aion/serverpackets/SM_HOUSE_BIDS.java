package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;
import java.util.function.Function;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.HouseBids;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.HousingBidService;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.taskmanager.tasks.housing.AuctionEndTask;

/**
 * @author Rolandas, Neon
 */
public class SM_HOUSE_BIDS extends AionServerPacket {

	public static final int STATIC_BODY_SIZE = 28;
	public static final Function<HouseBids, Integer> DYNAMIC_BODY_PART_SIZE_CALCULATOR = (bid) -> 44;

	private final boolean isFirst;
	private final boolean isLast;
	private final List<HouseBids> houseBids;

	public SM_HOUSE_BIDS(boolean isFirstPacket, boolean isLastPacket, List<HouseBids> houseBids) {
		isFirst = isFirstPacket;
		isLast = isLastPacket;
		this.houseBids = houseBids;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Player player = con.getActivePlayer();
		HouseBids.Bid lastBid = isLast ? HousingBidService.getInstance().findLastBid(player) : null;
		HouseBids bidsForRegisteredHouse = isLast ? HousingBidService.getInstance().findBidsForRegisteredHouse(player) : null;
		writeC(isFirst ? 1 : 0);
		writeC(isLast ? 1 : 0);

		writeD(lastBid == null ? 0 : lastBid.getListIndex());
		writeQ(lastBid == null ? 0 : lastBid.getKinah());

		writeD(bidsForRegisteredHouse == null ? 0 : bidsForRegisteredHouse.getListIndex());
		writeQ(bidsForRegisteredHouse == null ? 0 : bidsForRegisteredHouse.getInitialOffer().getKinah()); // starting price

		writeH(houseBids.size());
		for (HouseBids bids : houseBids) {
			House house = HousingService.getInstance().findHouse(bids.getHouseObjectId());
			writeD(bids.getListIndex());
			writeD(house.getLand().getId());
			writeD(house.getAddress().getId());
			writeD(house.getBuilding().getId());
			writeD(house.getHouseType().getId()); // client seems to ignore this
			writeQ(bids.getHighestBid().getKinah());
			writeQ(100000); // what's this? the same static value is sent in CM_REGISTER_HOUSE
			writeD(bids.getBidCount());
			writeD(AuctionEndTask.getInstance().getRemainingAuctionSeconds(bids.getHouseObjectId()));
		}
	}

}
