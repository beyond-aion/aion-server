package com.aionemu.gameserver.network.aion.clientpackets;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Set;

import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.MaintenanceTask;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_PAY_RENT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author Rolandas
 */
public class CM_HOUSE_PAY_RENT extends AionClientPacket {

	int weekCount;

	public CM_HOUSE_PAY_RENT(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		weekCount = readUC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (!HousingConfig.ENABLE_HOUSE_PAY) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_F2P_CASH_HOUSE_FEE_FREE());
			return;
		}

		House house = player.getActiveHouse();
		long toPay = house.getLand().getMaintenanceFee() * weekCount;
		if (toPay <= 0) {
			return;
		}
		if (player.getInventory().getKinah() < toPay) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_NOT_ENOUGH_MONEY());
			return;
		}

		long payTime = house.getNextPay() != null ? house.getNextPay().getTime() : (long) MaintenanceTask.getInstance().getRunTime() * 1000;
		int counter = weekCount;
		while ((--counter) >= 0) {
			payTime += MaintenanceTask.getInstance().getPeriod();
		}

		ZonedDateTime nextRun = ServerTime.ofEpochSecond(MaintenanceTask.getInstance().getRunTime());
		if (nextRun.plusWeeks(4).isBefore(ServerTime.ofEpochMilli(payTime))) { // client cap
			return;
		}

		player.getInventory().decreaseKinah(toPay);
		house.setNextPay(new Timestamp(payTime));
		house.setFeePaid(true);
		house.save();
		PacketSendUtility.sendPacket(player, new SM_HOUSE_PAY_RENT(weekCount));
	}
}
