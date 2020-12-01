package admincommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.HouseType;
import com.aionemu.gameserver.services.HousingBidService;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.utils.collections.Predicates;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Rolandas, Luzien, Neon
 */
public class Auction extends AdminCommand {

	public Auction() {
		super("auction", "Adds or removes houses to/from auction.");

		// @formatter:off
		setSyntaxInfo(
			"<address> [starting price] - Auctions the given house.",
			"<zone> <house type> <count> [starting price] - Auctions free houses of given type that are in the specified zone.",
			"asmo|ely <house type> <count> [starting price] - Auctions free asmodian or elysean houses of given type.",
			"end <address|zone> - Ends the auction for given house(s), transferring ownership to the highest bidder.",
			"cancel <address|zone> - Cancels the auction for given house(s).",
			"Zone: Zone name from zones xml files",
			"House type: house, mansion, estate, palace",
			"If no starting price is given, default will be taken from templates."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		boolean isCancel = "cancel".equalsIgnoreCase(params[0]);
		if ("end".equalsIgnoreCase(params[0]) || isCancel) {
			if (params.length < 2) {
				sendInfo(admin);
				return;
			}

			boolean isHouseAddress = params[1].matches("\\d+");
			List<House> houses;
			if (isHouseAddress) {
				House house = HousingService.getInstance().getHouseByAddress(Integer.parseInt(params[1]));
				if (house == null) {
					sendInfo(admin, "Invalid house address.");
					return;
				}
				houses = Collections.singletonList(house);
			} else {
				houses = findHousesInZone(admin, params[1], Predicates.alwaysTrue());
				if (houses == null)
					return;
			}

			int removedHouses = 0;
			for (House house : houses) {
				if (!isCancel && HousingBidService.getInstance().endAuction(house.getObjectId())
					|| isCancel && HousingBidService.getInstance().cancelAuction(house)) {
					sendInfo(admin,
						(isCancel ? "Canceled" : "Ended") + " auction for " + house.getHouseType().name().toLowerCase() + " " + house.getAddress().getId());
					removedHouses++;
				}
			}
			if (removedHouses == 0)
				sendInfo(admin, isHouseAddress ? "The house is not for sale." : "No houses for sale in this zone.");
		} else if (params[0].matches("\\d+")) {
			int address = Integer.parseInt(params[0]);
			House house = HousingService.getInstance().getHouseByAddress(address);
			if (house == null) {
				sendInfo(admin, "Invalid address.");
				return;
			}
			if (house.getBids() != null) {
				sendInfo(admin, "Address " + address + " is already in auction.");
				return;
			}
			long price = params.length < 2 ? house.getDefaultAuctionPrice() : Long.parseLong(params[1]);
			if (price <= 0) {
				sendInfo(admin, "Starting price must be positive.");
				return;
			}
			HousingBidService.getInstance().auction(house, price);
			sendInfo(admin, "Address " + address + " was auctioned successfully.");
		} else if ("add".equals(params[0])) {
			if (params.length < 4 || params.length > 5) {
				sendInfo(admin);
				return;
			}

			HouseType houseType = HouseType.valueOf(params[2].toUpperCase());
			int maxCount = Integer.parseInt(params[3]);
			if (maxCount <= 0) {
				sendInfo(admin, "Count must be positive.");
				return;
			}

			Predicate<House> filter = house -> house.getHouseType() == houseType && house.getBids() == null && house.getOwnerId() == 0;
			List<House> houses;
			if ("asmodians".startsWith(params[1].toLowerCase())) {
				filter = filter.and(house -> house.matchesLandRace(Race.ASMODIANS));
				houses = HousingService.getInstance().getCustomHouses().stream().filter(filter).collect(Collectors.toList());
			} else if ("elyos".startsWith(params[1].toLowerCase())) {
				filter = filter.and(house -> house.matchesLandRace(Race.ELYOS));
				houses = HousingService.getInstance().getCustomHouses().stream().filter(filter).collect(Collectors.toList());
			} else {
				houses = findHousesInZone(admin, params[1], filter);
			}
			if (houses == null)
				return;
			if (houses.isEmpty()) {
				sendInfo(admin, "No auctionable " + houseType.name().toLowerCase() + "s found.");
				return;
			}
			long price = params.length < 5 ? houses.get(0).getDefaultAuctionPrice() : Long.parseLong(params[4]);
			if (price <= 0) {
				sendInfo(admin, "Starting price must be positive.");
				return;
			}

			int counter = 0;
			Collections.shuffle(houses);
			for (House house : houses) {
				if (HousingBidService.getInstance().auction(house, price) && ++counter > maxCount)
					break;
			}

			sendInfo(admin, "Auctioned " + counter + " " + houseType.name().toLowerCase() + "s for a starting price of " + price + " Kinah.");
		} else {
			sendInfo(admin);
		}
	}

	private List<House> findHousesInZone(Player admin, String zoneName, Predicate<House> filter) {
		ZoneName zone = ZoneName.get(zoneName);
		if (zone == ZoneName.NONE) {
			sendInfo(admin, "Invalid zone name");
			return null;
		}
		List<House> housesToRemove = new ArrayList<>();
		for (House house : HousingService.getInstance().getCustomHouses()) {
			if (!filter.test(house))
				continue;
			if (house.getPosition().getMapRegion().isInsideZone(zone, house.getX(), house.getY(), house.getZ()))
				housesToRemove.add(house);
		}
		return housesToRemove;
	}
}
