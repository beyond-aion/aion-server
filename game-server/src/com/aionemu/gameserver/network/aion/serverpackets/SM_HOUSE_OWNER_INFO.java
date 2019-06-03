package com.aionemu.gameserver.network.aion.serverpackets;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;

import com.aionemu.gameserver.model.gameobjects.player.HouseOwnerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.HouseStatus;
import com.aionemu.gameserver.model.town.Town;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.HousingBidService;
import com.aionemu.gameserver.services.TownService;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author Rolandas
 */
public class SM_HOUSE_OWNER_INFO extends AionServerPacket {

	private Player player;
	private House activeHouse;
	private House inactiveHouse;

	public SM_HOUSE_OWNER_INFO(Player player) {
		this.player = player;
		for (House house : player.getHouses()) {
			if (house.getStatus() == HouseStatus.INACTIVE)
				inactiveHouse = house;
			else if (house.getStatus() != HouseStatus.NOSALE)
				activeHouse = house;
		}
		// Should not happen, just a validation
		if (activeHouse == null)
			inactiveHouse = null;
		if (inactiveHouse != null && activeHouse.getSellStarted() == null) {
			inactiveHouse = null;
		}
	}

	@Override
	protected void writeImpl(AionConnection con) {
		if (activeHouse == null) {
			writeD(0);
			writeD(player.hasHouseOwnerState(HouseOwnerState.BUY_STUDIO_ALLOWED) ? 355000 : 0); // studio building id
		} else {
			writeD(activeHouse.getAddress().getId());
			writeD(activeHouse.getBuilding().getId());
		}
		writeC(player.getHouseOwnerStates());
		int townLevel = 1;
		if (activeHouse != null && activeHouse.getAddress().getTownId() != 0) {
			Town town = TownService.getInstance().getTownById(activeHouse.getAddress().getTownId());
			townLevel = town.getLevel();
		}
		writeC(townLevel);
		writeD(calculateWeeksUntilNextPay()); // controls date and color in player profile house icon tooltip, as well as overdue pay amount when paying

		if (inactiveHouse == null) {
			writeD(0);
			writeD(0);
			writeD(0);
		} else {
			long timePassed = (HousingBidService.getInstance().getAuctionStartTime() - activeHouse.getSellStarted().getTime()) / 1000;
			int timeLeft = (int) (2 * 7 * 24 * 3600 - timePassed);
			if (timeLeft < 0)
				timeLeft = 0;
			writeD(inactiveHouse.getAddress().getId());
			writeD(inactiveHouse.getBuilding().getId());
			writeD(timeLeft);
		}
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
