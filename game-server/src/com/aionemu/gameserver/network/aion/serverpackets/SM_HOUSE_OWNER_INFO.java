package com.aionemu.gameserver.network.aion.serverpackets;

import java.sql.Timestamp;

import org.joda.time.DateTime;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerHouseOwnerFlags;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.HouseStatus;
import com.aionemu.gameserver.model.house.MaintenanceTask;
import com.aionemu.gameserver.model.town.Town;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.HousingBidService;
import com.aionemu.gameserver.services.TownService;

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
			writeD(player.isBuildingInState(PlayerHouseOwnerFlags.BUY_STUDIO_ALLOWED) ? 355000 : 0); // studio building id
		}
		else {
			writeD(activeHouse.getAddress().getId());
			writeD(activeHouse.getBuilding().getId());
		}
		writeC(player.getBuildingOwnerStates());
		int townLevel = 1;
		if (activeHouse != null && activeHouse.getAddress().getTownId() != 0) {
			Town town = TownService.getInstance().getTownById(activeHouse.getAddress().getTownId());
			townLevel = town.getLevel();
		}
		writeC(townLevel);
		// Maintenance bill weeks left ?, if 0 maintenance date is in red
		if (activeHouse == null || !activeHouse.isFeePaid()) {
			writeC(0);
		}
		else {
			Timestamp nextPay = activeHouse.getNextPay();
			float diff;
			if (nextPay == null) {
				// See MaintenanceTask.updateMaintainedHouses()
				// all just obtained houses have fee paid true and time is null;
				// means they should pay next week
				diff = MaintenanceTask.getInstance().getPeriod();
			}
			else {
				long paytime = activeHouse.getNextPay().getTime();
				diff = paytime - ((long) MaintenanceTask.getInstance().getRunTime() * 1000);
			}
			if (diff < 0) {
				writeC(0);
			}
			else {
				int weeks = (int) (Math.round(diff / MaintenanceTask.getInstance().getPeriod()));
				if (DateTime.now().getDayOfWeek() != 7) // Hack for auction Day, client counts sunday to new week
					weeks++;
				writeC(weeks);
			}
		}
		writeH(0); // unk
		writeC(0); // unk

		if (inactiveHouse == null) {
			writeD(0);
			writeD(0);
			writeD(0);
		}
		else {
			long timePassed = (HousingBidService.getInstance().getAuctionStartTime() - activeHouse.getSellStarted().getTime()) / 1000;
			int timeLeft = (int)(2 * 7 * 24 * 3600 - timePassed);
			if (timeLeft < 0)
				timeLeft = 0;
			writeD(inactiveHouse.getAddress().getId());
			writeD(inactiveHouse.getBuilding().getId());
			writeD(timeLeft);
		}
	}
}
