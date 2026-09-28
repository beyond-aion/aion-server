package admincommands;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.HouseType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Rolandas
 */
public class HouseCommand extends AdminCommand {

	public HouseCommand() {
		super("house", "House teleport and ownership management.", """
			list - Shows house addresses for each map.
			tp <address> - Teleports you to the house with the given address.
			own <address> - Gives ownership of given house to your target.
			revoke <address> - Revokes ownership of given house.
			reloadscripts <address> - Reloads all scripts for the given house.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length >= 1 && "list".equalsIgnoreCase(params[0])) {
			listHouses(admin);
		} else if (params.length >= 2 && "own".equalsIgnoreCase(params[0])) {
			acquireHouse(admin, getHouse(params[1]));
		} else if (params.length >= 2 && "revoke".equalsIgnoreCase(params[0])) {
			revokeOwnership(admin, getHouse(params[1]));
		} else if (params.length >= 2 && "tp".equalsIgnoreCase(params[0])) {
			House house = getHouse(params[1]);
			TeleportService.teleportTo(admin, house.getWorldMapInstance(), house.getX(), house.getY(), house.getZ(), house.getTeleportHeading());
		} else if (params.length >= 2 && "reloadscripts".equalsIgnoreCase(params[0])) {
			reloadPlayerScripts(admin, getHouse(params[1]));
		} else {
			sendInfo(admin);
		}
	}

	private void listHouses(Player admin) {
		HousingService.getInstance().getCustomHouses().stream()
			.collect(Collectors.groupingBy(house -> house.getAddress().getMapId(), TreeMap::new, Collectors.toList()))
			.forEach((mapId, houses) -> {
				sendInfo(admin, "House addresses in " + worldName(mapId) + ":");
				groupByType(houses).forEach((houseType, housesOfType) -> {
					String houseTypeName = houseType.name().charAt(0) + houseType.name().substring(1).toLowerCase();
					sendInfo(admin, "\t" + houseTypeName + ": " + formatAddresses(housesOfType));
			});
		});
	}

	private String formatAddresses(List<House> houses) {
		boolean dash = false;
		int lastAddress = houses.getFirst().getAddress().getId();
		String addresses = ChatUtil.color(lastAddress + "", Color.WHITE);
		for (int i = 1; i < houses.size(); i++) {
			House house = houses.get(i);
			int currentAddress = house.getAddress().getId();
			if (lastAddress + 1 != currentAddress || i + 1 == houses.size() || currentAddress + 1 != houses.get(i + 1).getAddress().getId()) {
				if (!addresses.isEmpty() && !dash)
					addresses += ", ";
				addresses += ChatUtil.color(currentAddress + "", Color.WHITE);
				dash = false;
			} else if (!dash) {
				addresses += "-";
				dash = true;
			}
			lastAddress = house.getAddress().getId();
		}
		return addresses;
	}

	private Map<HouseType, List<House>> groupByType(List<House> houses) {
		return houses.stream()
			.sorted(Comparator.comparing((House house) -> house.getHouseType().getId()).reversed().thenComparing(house -> house.getAddress().getId()))
			.collect(Collectors.groupingBy(House::getHouseType, LinkedHashMap::new, Collectors.toList()));
	}

	private House getHouse(String param) {
		int address = Integer.parseInt(param);
		return Objects.requireNonNull(HousingService.getInstance().getHouseByAddress(address), "Invalid address.");
	}

	private void acquireHouse(Player admin, House house) {
		VisibleObject creature = admin.getTarget();
		if (!(creature instanceof Player target)) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return;
		}

		if (house.getOwnerId() == target.getObjectId()) {
			sendInfo(admin, name(target) + " already owns that house.");
			return;
		}
		if (target.getHouses().size() >= 2) {
			sendInfo(admin, name(target) + " must sell his old house which is currently in grace time first!");
			return;
		}
		House studio = HousingService.getInstance().getPlayerStudio(target.getObjectId());
		if (studio != null)
			HousingService.getInstance().changeOwner(studio, 0);
		HousingService.getInstance().changeOwner(house, target.getObjectId());
		sendInfo(admin, "House " + house.getName() + " is now owned by " + name(target));
	}

	private void revokeOwnership(Player admin, House house) {
		int ownerId = house.getOwnerId();
		if (ownerId == 0) {
			sendInfo(admin, "House has no owner.");
			return;
		}
		HousingService.getInstance().changeOwner(house, 0);
		sendInfo(admin, "Ownership of house " + house.getAddress().getId() + " was revoked from " + PlayerService.getPlayerName(ownerId));
	}

	private void reloadPlayerScripts(Player admin, House house) {
		Npc butler = house.getButler();
		if (butler == null) {
			sendInfo(admin, "No butler was found for house with address " + house.getAddress().getId());
			return;
		}
		house.reloadPlayerScripts();
		butler.getKnownList().forEachPlayer(house::sendScripts);
		sendInfo(admin, "Script reload successful");
	}

}
