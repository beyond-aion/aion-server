package com.aionemu.gameserver.services;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.dao.HouseBidsDAO;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Letter;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.HouseBids;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RECEIVE_BIDS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.mail.AuctionResult;
import com.aionemu.gameserver.services.mail.MailFormatter;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.taskmanager.tasks.housing.AuctionEndTask;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author Rolandas, Neon
 */
public class HousingBidService {

	private static final Logger log = LoggerFactory.getLogger("HOUSE_AUCTION_LOG");
	private static final HousingBidService instance = new HousingBidService();
	private final Map<Integer, HouseBids> bids = new ConcurrentHashMap<>();

	private HousingBidService() {
		Set<Integer> deletedPlayerIds = HouseBidsDAO.loadBids(bids);
		deletedPlayerIds.forEach(this::disableBids);
		setBidInfoToHouses();
		log.info("Loaded bids for " + bids.size() + " houses");
	}

	private void setBidInfoToHouses() {
		HousingService.getInstance().getCustomHouses().forEach(house -> {
			house.setBids(getBidInfo(house), true);
			if (house.getBids() != null && house.isInactive())
				log.warn(house + " is for auction but inactive.");
		});
	}

	public static HousingBidService getInstance() {
		return instance;
	}

	public boolean isRegisteringAllowed() {
		if (!HousingConfig.ENABLE_HOUSE_AUCTIONS)
			return false;
		int today = ServerTime.now().getDayOfWeek().getValue();
		int from = HousingConfig.HOUSE_AUCTION_REGISTER_DAYS[0];
		int to = HousingConfig.HOUSE_AUCTION_REGISTER_DAYS[1];
		if (from > to) // e.g. saturday (6) to wednesday (3)
			return from <= today || to >= today;
		else // e.g. monday (1) to friday (5)
			return from <= today && to >= today;
	}

	public boolean auction(House house, long initialPrice) {
		HouseBids houseBids = new HouseBids(house.getObjectId(), initialPrice);
		HouseBids.Bid bid = houseBids.getHighestBid();
		if (bids.putIfAbsent(house.getObjectId(), houseBids) != null)
			return false;
		if (house.getPersistentState() == Persistable.PersistentState.NEW) // house must exist in DB before saving a bid due to foreign key
			house.save();
		if (!HouseBidsDAO.addBid(bid)) {
			bids.remove(house.getObjectId(), houseBids);
			return false;
		}
		house.setBids(houseBids, true);
		house.getController().updateSign();
		house.getController().updateAppearance();
		return true;
	}

	private boolean isAuctionOpen(int houseObjectId) {
		return bids.containsKey(houseObjectId) && isBiddingTime(houseObjectId);
	}

	private boolean isBiddingTime(int houseObjectId) {
		ZonedDateTime now = ServerTime.now();
		return now.getDayOfWeek() != DayOfWeek.SUNDAY || now.getHour() < 12 || AuctionEndTask.getInstance().isAuctionProlonged(houseObjectId);
	}

	public HouseBids getBidInfo(House house) {
		return bids.get(house.getObjectId());
	}

	public List<HouseBids> getBidInfo(Race race) {
		List<HouseBids> houseBids = new ArrayList<>();
		for (HouseBids bidInfo : bids.values()) {
			if (HousingService.getInstance().findHouse(bidInfo.getHouseObjectId()).matchesLandRace(race))
				houseBids.add(bidInfo);
		}
		return houseBids;
	}

	public HouseBids.Bid bid(Player player, int listIndex, long bidOffer) {
		HouseBids houseBids = bids.values().stream().filter(b -> b.getListIndex() == listIndex).findAny().orElse(null);
		if (!isAllowedToBid(player, houseBids, bidOffer))
			return null; // bid too low or bidding not allowed
		HouseBids.Bid previousBid = houseBids.getHighestBid();
		HouseBids.Bid bid = houseBids.bid(player, bidOffer);
		if (bid == null) { // another bidder just bid more
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_BID_LOWER());
			PacketSendUtility.sendPacket(player, new SM_RECEIVE_BIDS(0));
			return null;
		}
		if (AuctionEndTask.getInstance().tryProlongAuction(bid.getHouseObjectId()))
			HouseBidsDAO.addBid(bid); // no need to save the bid if prolongation failed (the auction just ended)
		player.getInventory().decreaseKinah(bid.getKinah());
		House bidHouse = HousingService.getInstance().findHouse(bid.getHouseObjectId());
		if (previousBid != houseBids.getInitialOffer() && previousBid.getPlayerObjectId() != 0) {
			PlayerCommonData prevPcd = PlayerService.getOrLoadPlayerCommonData(previousBid.getPlayerObjectId());
			if (prevPcd.isOnline()) {
				PacketSendUtility.sendPacket(prevPcd.getPlayer(), SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_BID_CANCEL());
				PacketSendUtility.sendPacket(prevPcd.getPlayer(), new SM_RECEIVE_BIDS(0));
			}
			MailFormatter.sendHouseAuctionMail(bidHouse, prevPcd, AuctionResult.FAILED_BID, bid.getTime(), previousBid.getKinah());
		}
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_BID_SUCCESS(bidHouse.getAddress().getId()));
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_PRICE_CHANGE(bidOffer));
		PacketSendUtility.sendPacket(player, new SM_RECEIVE_BIDS(0));
		return bid;
	}

	private boolean isAllowedToBid(Player player, HouseBids houseBids, long bidOffer) {
		if (!HousingService.getInstance().canOwnHouse(player, true))
			return false;
		if (houseBids == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_BID_FAIL());
			return false;
		}
		if (!isAuctionOpen(houseBids.getHouseObjectId())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_BID_TIMEOUT());
			return false;
		}
		House bidHouse = HousingService.getInstance().findHouse(houseBids.getHouseObjectId());
		if (player.getObjectId() == bidHouse.getOwnerId()) { // client usually already checks this, but we want to make sure
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_BID_MY_HOUSE());
			return false;
		}

		if (HousingService.getInstance().findInactiveHouse(player.getObjectId()) != null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_BID_GRACE_HOUSE());
			return false;
		}
		House playerHouse = player.getActiveHouse();
		if (playerHouse != null && !playerHouse.isFeePaid() && HousingConfig.ENABLE_HOUSE_PAY) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_BID_OVERDUE());
			return false;
		}
		int minBidLevel = getMinBidLevel(bidHouse);
		if (player.getLevel() < minBidLevel) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_BID_LOW_LEVEL(minBidLevel));
			return false;
		}
		HouseBids.Bid highestBid = houseBids.getHighestBid();
		if (highestBid.getPlayerObjectId() == player.getObjectId()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_BID_SUCC_BID_HOUSE());
			return false;
		}
		if (bids.values().stream().anyMatch(b -> b.isHighestBidder(player))) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_BID_OTHER_HOUSE());
			return false;
		}
		if (player.getInventory().getKinah() < bidOffer) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_BID_NOT_ENOUGH_MONEY(bidOffer));
			return false;
		}
		long currentBid = highestBid.getKinah();
		if (bidOffer - currentBid >= currentBid * HousingConfig.AUCTION_BID_STEP_LIMIT / 100f) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_BID_EXCESS_ACCOUNT());
			return false;
		}
		return true;
	}

	public void endAuctions() {
		bids.keySet().forEach(houseObjectId -> {
			if (!AuctionEndTask.getInstance().isAuctionProlonged(houseObjectId))
				endAuction(houseObjectId);
		});
		impoundAndAuctionOldPlayerHouses();
	}

	public boolean endAuction(int houseObjectId) {
		AuctionEndTask.getInstance().onAuctionEnd(houseObjectId);
		HouseBids bids;
		if (!HouseBidsDAO.deleteHouseBids(houseObjectId) || (bids = this.bids.remove(houseObjectId)) == null)
			return false;
		House house = HousingService.getInstance().findHouse(houseObjectId);
		house.setBids(null, false);
		int sellerId = house.getOwnerId();
		PlayerCommonData sellerPcd = sellerId == 0 ? null : PlayerService.getOrLoadPlayerCommonData(sellerId);
		HouseBids.Bid highestBid = bids.getHighestBid();
		if (highestBid == bids.getInitialOffer()) {
			AuctionResult result = AuctionResult.FAILED_SALE;
			long time = bids.getInitialOffer().getTime(); // registration time
			long compensation = 0;

			House inactiveHouse = sellerId == 0 ? null : HousingService.getInstance().findInactiveHouse(sellerId);
			if (inactiveHouse != null && inactiveHouse.secondsUntilGraceEnd() == 0) {
				HousingService.getInstance().changeOwner(house, 0); // inactive house will also be activated automatically by this
				result = AuctionResult.GRACE_FAIL;
				time = System.currentTimeMillis();
				compensation = (long) (bids.getInitialOffer().getKinah() * HousingConfig.AUCTION_GRACE_END_REFUND_PERCENT);
			} else {
				house.getController().updateSign();
			}
			if (sellerPcd != null) {
				if (sellerPcd.isOnline())
					PacketSendUtility.sendPacket(sellerPcd.getPlayer(), SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_AUCTION_FAIL(house.getAddress().getId()));
				MailFormatter.sendHouseAuctionMail(house, sellerPcd, result, time, compensation);
			}
			if (LoggingConfig.LOG_HOUSE_AUCTION) {
				log.info("Address " + house.getAddress().getId() + " not sold for " + bids.getInitialOffer().getKinah() + " kinah (result: " + result
					+ "; return: " + compensation + " kinah)");
			}
		} else {
			PlayerCommonData buyerPcd = PlayerService.getOrLoadPlayerCommonData(highestBid.getPlayerObjectId());

			if (buyerPcd == null) {
				if (highestBid.getPlayerObjectId() == 0)
					log.info(house + " wasn't sold because the winning bidder deleted his character.");
				else
					log.warn(house + " could not be sold to player " + highestBid.getPlayerObjectId() + " because the player couldn't be found");
				house.getController().updateSign();
				return true;
			}
			if (buyerPcd.getPlayerObjId() == sellerId) {
				log.warn("Sold " + house + " to its own owner (" + sellerId + "), cancelling!");
				house.getController().updateSign();
				return true;
			}

			House studio = HousingService.getInstance().getPlayerStudio(buyerPcd.getPlayerObjId());
			if (studio != null)
				HousingService.getInstance().changeOwner(studio, 0);
			HousingService.getInstance().changeOwner(house, buyerPcd.getPlayerObjId());

			AuctionResult result = AuctionResult.WIN_BID;
			long time = System.currentTimeMillis();
			if (buyerPcd.isOnline())
				PacketSendUtility.sendPacket(buyerPcd.getPlayer(), SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_BID_WIN(house.getAddress().getId()));
			if (house.isInactive()) { // buyer has another house
				if (buyerPcd.isOnline())
					PacketSendUtility.sendPacket(buyerPcd.getPlayer(), SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_GRACE_START(house.getAddress().getId()));
				result = AuctionResult.GRACE_START;
				MailFormatter.sendHouseAuctionMail(house, buyerPcd, result, System.currentTimeMillis() + house.secondsUntilGraceEnd() * 1000, 0);
			} else {
				MailFormatter.sendHouseAuctionMail(house, buyerPcd, result, time, 0);
			}

			if (sellerPcd != null) {
				if (sellerPcd.isOnline())
					PacketSendUtility.sendPacket(sellerPcd.getPlayer(), SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_AUCTION_SUCCESS(house.getAddress().getId()));
				House newHouse = HousingService.getInstance().findActiveHouse(sellerPcd.getPlayerObjId());
				if (newHouse != null) { // seller got his new house activated because the old one is sold
					if (sellerPcd.isOnline())
						PacketSendUtility.sendPacket(sellerPcd.getPlayer(), SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_GRACE_SUCCESS(house.getAddress().getId()));
					MailFormatter.sendHouseAuctionMail(newHouse, sellerPcd, AuctionResult.GRACE_SUCCESS, time, highestBid.calculateSaleRewardKinah());
				} else
					MailFormatter.sendHouseAuctionMail(house, sellerPcd, AuctionResult.SUCCESS_SALE, time, highestBid.calculateSaleRewardKinah());
			}

			if (LoggingConfig.LOG_HOUSE_AUCTION) {
				String sellerInfo = sellerPcd == null ? "" : " by player " + sellerPcd.getPlayerObjId();
				log.info("Address " + house.getAddress().getId() + " sold" + sellerInfo + " for " + highestBid.getKinah() + " kinah (" + bids.getBidCount()
					+ " bids; result: " + result + ") to player " + buyerPcd.getPlayerObjId());
			}
		}
		return true;
	}

	private void impoundAndAuctionOldPlayerHouses() {
		for (House house : HousingService.getInstance().getCustomHouses()) {
			if (house.isInactive() && house.secondsUntilGraceEnd() == 0) {
				House oldHouse = HousingService.getInstance().findActiveHouse(house.getOwnerId());
				HousingService.getInstance().changeOwner(oldHouse, 0);
				PlayerCommonData pcd = PlayerService.getOrLoadPlayerCommonData(house.getOwnerId());
				if (pcd.isOnline())
					PacketSendUtility.sendPacket(pcd.getPlayer(),
						SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_GRACE_FAIL(house.getAddress().getId(), oldHouse.getAddress().getId()));
				if (auction(oldHouse, oldHouse.getDefaultAuctionPrice()))
					MailFormatter.sendHouseAuctionMail(house, pcd, AuctionResult.GRACE_FAIL, System.currentTimeMillis(),
						(long) (oldHouse.getDefaultAuctionPrice() * HousingConfig.AUCTION_GRACE_END_REFUND_PERCENT));
			}
		}
	}

	private int getMinBidLevel(House house) {
		switch (house.getHouseType()) {
			case HOUSE:
				if (HousingConfig.HOUSE_MIN_BID_LEVEL > 0)
					return HousingConfig.HOUSE_MIN_BID_LEVEL;
				break;
			case MANSION:
				if (HousingConfig.MANSION_MIN_BID_LEVEL > 0)
					return HousingConfig.MANSION_MIN_BID_LEVEL;
				break;
			case ESTATE:
				if (HousingConfig.ESTATE_MIN_BID_LEVEL > 0)
					return HousingConfig.ESTATE_MIN_BID_LEVEL;
				break;
			case PALACE:
				if (HousingConfig.PALACE_MIN_BID_LEVEL > 0)
					return HousingConfig.PALACE_MIN_BID_LEVEL;
				break;
		}
		return house.getLand().getSaleOptions().getMinLevel();
	}

	public void disableBids(int playerObjId) {
		List<HouseBids.Bid> deletedBids = new ArrayList<>();
		bids.values().forEach(b -> deletedBids.addAll(b.deleteOrDisableBids(playerObjId)));
		HouseBidsDAO.deleteOrDisableBids(playerObjId, deletedBids);
	}

	public HouseBids.Bid findLastBid(Player player) {
		return bids.values().stream().map(bids -> bids.getLatestBid(player)).filter(Objects::nonNull)
			.reduce(BinaryOperator.maxBy(Comparator.comparing(HouseBids.Bid::getTime))).orElse(null);
	}

	public HouseBids findBidsForRegisteredHouse(Player player) {
		for (House house : player.getHouses()) {
			if (house.getBids() != null)
				return house.getBids();
		}
		return null;
	}

	public boolean cancelAuction(House house) {
		HouseBids bids = this.bids.remove(house.getObjectId());
		if (bids == null)
			return false;

		HouseBidsDAO.deleteHouseBids(house.getObjectId());
		house.setBids(null, true);
		house.getController().updateSign();
		house.getController().updateAppearance();

		if (house.getOwnerId() != 0) {
			PlayerCommonData pcd = PlayerService.getOrLoadPlayerCommonData(house.getOwnerId());
			if (pcd.isOnline()) {
				PacketSendUtility.sendPacket(pcd.getPlayer(), SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_AUCTION_FAIL(house.getAddress().getId()));
				PacketSendUtility.sendPacket(pcd.getPlayer(), new SM_RECEIVE_BIDS(1));
			}
			MailFormatter.sendHouseAuctionMail(house, pcd, AuctionResult.CANCELED_BID, System.currentTimeMillis(), 0);
		}
		HouseBids.Bid highestBid = bids.getHighestBid();
		if (highestBid != bids.getInitialOffer() && highestBid.getPlayerObjectId() != 0) {
			// return bid price only to the last bidder (previous bidders already get their money back when another player bids more)
			PlayerCommonData pcd = PlayerService.getOrLoadPlayerCommonData(highestBid.getPlayerObjectId());
			MailFormatter.sendHouseAuctionMail(house, pcd, AuctionResult.CANCELED_BID, System.currentTimeMillis(), highestBid.getKinah());
		}

		return true;
	}

	/**
	 * Notify once about new auction results, based on system mail checks and login time
	 */
	public void onPlayerLogin(Player player) {
		List<Letter> letters = player.getMailbox().getNewSystemLetters("$$HS_AUCTION_MAIL");
		boolean needsRefresh = false;

		for (Letter letter : letters) {
			String[] titleParts = letter.getTitle().split(",");
			String[] bodyParts = letter.getMessage().split(",");
			AuctionResult result = AuctionResult.getResultFromId(Integer.parseInt(titleParts[0]));
			if (result == AuctionResult.FAILED_BID) {
				needsRefresh = true;
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_BID_CANCEL());
			} else if (result == AuctionResult.WIN_BID || result == AuctionResult.GRACE_START) {
				needsRefresh = true;
				int address = Integer.parseInt(bodyParts[1]);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_BID_WIN(address));
			} else if (result == AuctionResult.FAILED_SALE) {
				needsRefresh = true;
				int address = Integer.parseInt(bodyParts[1]);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_AUCTION_FAIL(address));
			} else if (result == AuctionResult.SUCCESS_SALE || result == AuctionResult.GRACE_SUCCESS) {
				needsRefresh = true;
				int address = Integer.parseInt(bodyParts[1]);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_AUCTION_SUCCESS(address));
			}
		}

		if (needsRefresh)
			PacketSendUtility.sendPacket(player, new SM_RECEIVE_BIDS(0));
	}
}
