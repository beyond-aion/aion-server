package com.aionemu.gameserver.model.house;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Neon
 */
public class HouseBids {

	private static final AtomicInteger counter = new AtomicInteger();
	private final int listIndex;
	private final int houseObjectId;
	private final long registrationFee;
	private final List<Bid> bids = new ArrayList<>();

	public HouseBids(int houseObjectId, long initialPrice) {
		this(houseObjectId, initialPrice, System.currentTimeMillis());
	}

	public HouseBids(int houseObjectId, long initialPrice, long time) {
		this.listIndex = counter.incrementAndGet();
		this.houseObjectId = houseObjectId;
		this.registrationFee = (long) (initialPrice * HousingConfig.AUCTION_REGISTRATION_FEE_PERCENT);
		bids.add(new Bid(0, initialPrice, time));
	}

	public int getListIndex() {
		return listIndex;
	}

	public int getHouseObjectId() {
		return houseObjectId;
	}

	/**
	 * @return Players bid if bidding was successful, meaning he is the highest bidder.
	 */
	public Bid bid(Player player, long bidKinah) {
		return bid(player.getObjectId(), bidKinah, System.currentTimeMillis());
	}

	/**
	 * @return Players bid if bidding was successful, meaning he is the highest bidder.
	 */
	public synchronized Bid bid(int playerObjectId, long bidKinah, long time) {
		Bid highestBid = getHighestBid();
		if (highestBid.getKinah() < bidKinah || highestBid == getInitialOffer() && highestBid.getKinah() == bidKinah) {
			Bid bid = new Bid(playerObjectId, bidKinah, time);
			bids.add(bid);
			return bid;
		}
		return null;
	}

	public boolean isHighestBidder(Player player) {
		return getHighestBid().getPlayerObjectId() == player.getObjectId();
	}

	public synchronized Bid getHighestBid() {
		return bids.get(bids.size() - 1);
	}

	public synchronized Bid getLatestBid(Player player) {
		for (int i = bids.size() - 1; i >= 0; i--) {
			Bid bid = bids.get(i);
			if (bid.getPlayerObjectId() == player.getObjectId())
				return bid;
		}
		return null;
	}

	public synchronized Bid getInitialOffer() {
		return bids.get(0);
	}

	public synchronized int getBidCount() {
		return bids.size() - 1; // first bid is initialPrice
	}

	/**
	 * Deletes all bids of given player which are not the highest bid. Disables the bid by setting bidder ID to 0 if it's the highest bid, because
	 * otherwise another bidder may become the highest bidder for two houses. Auction winner with bidder ID 0 will be handled gracefully on auction end.
	 */
	public synchronized List<Bid> deleteOrDisableBids(int playerObjectId) {
		List<Bid> bidsToDelete = new ArrayList<>();
		for (int i = 1, indexOfHighestBid = bids.size() - 1; i <= indexOfHighestBid; i++) {
			Bid bid = bids.get(i);
			if (bid.getPlayerObjectId() == playerObjectId) {
				if (i == 1 || i < indexOfHighestBid)
					bidsToDelete.add(bid);
				else 
					bid.playerObjectId = 0;
			}
		}
		bids.removeAll(bidsToDelete);
		return bidsToDelete;
	}

	public class Bid {

		private int playerObjectId;
		private final long kinah;
		private final long time;

		private Bid(int playerObjectId, long kinah, long time) {
			this.playerObjectId = playerObjectId;
			this.kinah = kinah;
			this.time = time;
		}

		public int getListIndex() {
			return listIndex;
		}

		public int getHouseObjectId() {
			return houseObjectId;
		}

		/**
		 * @return The player object ID of this bid. 0 if it's the initial offer or the player got deleted.
		 */
		public int getPlayerObjectId() {
			return playerObjectId;
		}

		public long getKinah() {
			return kinah;
		}

		public long getTime() {
			return time;
		}

		public long calculateSalesCommission() {
			return (long) (kinah * HousingConfig.AUCTION_SALES_COMMISION_PERCENT);
		}

		public long calculateSaleRewardKinah() {
			return kinah - calculateSalesCommission() + registrationFee;
		}
	}
}
