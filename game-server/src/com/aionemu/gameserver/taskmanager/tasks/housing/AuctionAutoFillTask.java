package com.aionemu.gameserver.taskmanager.tasks.housing;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.HouseBids;
import com.aionemu.gameserver.model.templates.housing.HouseType;
import com.aionemu.gameserver.services.HousingBidService;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.taskmanager.AbstractCronTask;

/**
 * Handles registering unoccupied houses automatically for auction.
 * 
 * @author Neon
 */
public class AuctionAutoFillTask extends AbstractCronTask {

	private static final AuctionAutoFillTask instance = new AuctionAutoFillTask();

	public static AuctionAutoFillTask getInstance() {
		return instance;
	}

	private AuctionAutoFillTask() {
		super(HousingConfig.AUCTION_AUTO_FILL_TIME);
	}

	@Override
	protected void executeTask() {
		if (HousingConfig.ENABLE_HOUSE_AUCTIONS) {
			autoFillAuction(Race.ELYOS);
			autoFillAuction(Race.ASMODIANS);
		}
	}

	private void autoFillAuction(Race race) {
		Set<House> auctionedHouses = findAuctionedHouses(race);
		List<House> auctionableHouses = findAuctionableHouses(race, auctionedHouses);
		Collections.shuffle(auctionableHouses);
		Map<HouseType, Long> auctionedHouseCounts = auctionedHouses.stream().collect(Collectors.groupingBy(House::getHouseType, Collectors.counting()));
		int[] added = { 0 };
		HousingConfig.AUCTION_AUTO_FILL_LIMITS.forEach((houseType, limit) -> {
			for (long auctioned = auctionedHouseCounts.getOrDefault(houseType, 0L); auctioned < limit; auctioned++) {
				House house = findAndRemoveHouse(auctionableHouses, houseType);
				if (house == null || !HousingBidService.getInstance().auction(house, house.getDefaultAuctionPrice()))
					break;
				added[0]++;
			}
		});
		log.info("[" + race + "] Added " + added[0] + " new houses automatically to auction.");
	}

	private Set<House> findAuctionedHouses(Race race) {
		return HousingBidService.getInstance().getBidInfo(race).stream().map(this::toHouse).collect(Collectors.toSet());
	}

	private House toHouse(HouseBids houseBids) {
		return HousingService.getInstance().findHouse(houseBids.getHouseObjectId());
	}

	private List<House> findAuctionableHouses(Race race, Set<House> auctionedHouses) {
		List<House> houses = HousingService.getInstance().getCustomHouses();
		houses.removeIf(house -> house.getOwnerId() != 0 || auctionedHouses.contains(house) || !house.matchesLandRace(race));
		return houses;
	}

	private House findAndRemoveHouse(List<House> houses, HouseType houseType) {
		for (Iterator<House> iterator = houses.iterator(); iterator.hasNext();) {
			House house = iterator.next();
			if (house.getHouseType() == houseType) {
				iterator.remove();
				return house;
			}
		}
		return null;
	}
}
