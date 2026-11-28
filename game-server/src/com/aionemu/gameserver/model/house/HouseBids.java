package com.aionemu.gameserver.model.house;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Neon
 */
public class HouseBids {

	/**
	 * The minimum raise the client sends when a player registers a house, and the one of auctions the server starts.
	 */
	public static final long DEFAULT_MINIMUM_RAISE = 100_000;
	/**
	 * Sanity cap for amounts a (modified) client may send: retail checks no maximum bid at all, neither server nor client side. The cap keeps every
	 * raise, fee, refund and kinah credit below inside a safe long range.
	 */
	public static final long MAXIMUM_BID = 9_999_999_999L;
	private static final AtomicInteger counter = new AtomicInteger();
	private final int listIndex;
	private final int houseObjectId;
	private final long registrationFee;
	private final long minimumRaise;
	private final List<Bid> bids = new ArrayList<>();

	public HouseBids(int houseObjectId, long initialPrice, long minimumRaise) {
		this(houseObjectId, initialPrice, minimumRaise, System.currentTimeMillis());
	}

	public HouseBids(int houseObjectId, long initialPrice, long minimumRaise, long time) {
		this.listIndex = counter.incrementAndGet();
		this.houseObjectId = houseObjectId;
		this.registrationFee = (long) (initialPrice * HousingConfig.AUCTION_REGISTRATION_FEE_PERCENT);
		this.minimumRaise = minimumRaise;
		bids.add(new Bid(0, initialPrice, time));
	}

	public int getListIndex() {
		return listIndex;
	}

	public int getHouseObjectId() {
		return houseObjectId;
	}

	public long getMinimumRaise() {
		return minimumRaise;
	}

	/**
	 * @return The least a bid must offer: the starting price for the first bid, then the highest bid plus the minimum raise.
	 */
	public synchronized long getMinimumBid() {
		Bid highestBid = getHighestBid();
		long highest = highestBid.getKinah();
		if (highestBid == getInitialOffer())
			return highest;
		return minimumRaise > MAXIMUM_BID - highest ? MAXIMUM_BID + 1 : highest + minimumRaise;
	}

	/**
	 * @return Players bid if bidding was successful, meaning he is the highest bidder.
	 */
	public synchronized Bid bid(Player player, long bidKinah) {
		if (!isValidAuctionAmount(bidKinah) || bidKinah < getMinimumBid())
			return null;
		return restoreBid(player.getObjectId(), bidKinah, System.currentTimeMillis());
	}

	public static boolean isValidAuctionAmount(long amount) {
		return amount > 0 && amount <= MAXIMUM_BID;
	}

	/**
	 * Adds a bid that was accepted before, in the order they were made.
	 */
	public synchronized Bid restoreBid(int playerObjectId, long bidKinah, long time) {
		Bid bid = new Bid(playerObjectId, bidKinah, time);
		bids.add(bid);
		return bid;
	}

	public boolean isHighestBidder(Player player) {
		return getHighestBid().getPlayerObjectId() == player.getObjectId();
	}

	public synchronized Bid getHighestBid() {
		return bids.getLast();
	}

	public synchronized Bid getLatestBid(Player player) {
		for (int i = bids.size() - 1; i >= 0; i--) {
			Bid bid = bids.get(i);
			if (bid.getPlayerObjectId() == player.getObjectId())
				return bid;
		}
		return null;
	}

	public synchronized Set<Integer> getBidderIds() {
		Set<Integer> bidderIds = new HashSet<>();
		for (int i = 1; i < bids.size(); i++)
			bidderIds.add(bids.get(i).getPlayerObjectId());
		bidderIds.remove(0);
		return bidderIds;
	}

	public synchronized Bid getInitialOffer() {
		return bids.getFirst();
	}

	public synchronized boolean removeBid(Bid bid) {
		return bids.remove(bid);
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

		public long getMinimumRaise() {
			return minimumRaise;
		}

		public long calculateSalesCommission() {
			return (long) (kinah * HousingConfig.AUCTION_SALES_COMMISION_PERCENT);
		}

		public long calculateSaleRewardKinah() {
			return kinah - calculateSalesCommission() + registrationFee;
		}
	}
}
