package com.aionemu.gameserver.network.aion.serverpackets;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;

import com.aionemu.gameserver.model.gameobjects.player.HouseOwnerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author Rolandas, Neon
 */
public class SM_HOUSE_OWNER_INFO extends AionServerPacket {

	private int playerHouseOwnerState;
	private House activeHouse;
	private House inactiveHouse;

	public SM_HOUSE_OWNER_INFO(Player player) {
		for (House house : player.getHouses()) {
			if (house.isInactive())
				inactiveHouse = house;
			else
				activeHouse = house;
		}
		if (activeHouse == null) {
			playerHouseOwnerState = HouseOwnerState.SINGLE_HOUSE.getId();
			if (HousingService.getInstance().canOwnHouse(player, false))
				playerHouseOwnerState |= HouseOwnerState.BIDDING_ALLOWED.getId();
		} else {
			playerHouseOwnerState = HouseOwnerState.HAS_OWNER.getId() | HouseOwnerState.BIDDING_ALLOWED.getId();
		}
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(activeHouse == null ? 0 : activeHouse.getAddress().getId());
		writeD(activeHouse == null ? 0 : activeHouse.getBuilding().getId());
		writeC(playerHouseOwnerState); // (SINGLE_HOUSE | BIDDING_ALLOWED) enables studio buy button at Parrine and disables house icon in profile
		writeC(activeHouse == null ? 0 : activeHouse.getTownLevel());
		writeD(calculateWeeksUntilNextPay()); // controls date and color in player profile house icon tooltip, as well as overdue pay amount when paying
		writeD(inactiveHouse == null ? 0 : inactiveHouse.getAddress().getId());
		writeD(inactiveHouse == null ? 0 : inactiveHouse.getBuilding().getId());
		writeD(inactiveHouse == null ? 0 : inactiveHouse.secondsUntilGraceEnd()); // seconds until new house (inactiveHouse) will be activated / old one (activeHouse) gets removed
	}

	private int calculateWeeksUntilNextPay() {
		int weeks = 0;
		if (activeHouse != null) {
			if (activeHouse.getNextPay() == null) { // newly acquired houses have one week free, nextPay will be set on the next house maintenance
				weeks = 1;
			} else {
				ZonedDateTime now = ServerTime.now();
				boolean isSundayAfterAuction = now.getDayOfWeek() == DayOfWeek.SUNDAY && now.getHour() >= 12;
				long days = Duration.between(now.toInstant(), activeHouse.getNextPay().toInstant()).toDays();
				weeks = (int) (days / 7);
				if (days < 0 && isSundayAfterAuction) // workaround for auction day, client counts sunday afternoon to new week
					weeks--;
				else if (days >= 0 && !isSundayAfterAuction)
					weeks++;
			}
		}
		return weeks;
	}
}
