package com.aionemu.gameserver.network.aion.clientpackets;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;

import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_PAY_RENT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.taskmanager.tasks.housing.MaintenanceTask;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author Rolandas
 */
public class CM_HOUSE_PAY_RENT extends AionClientPacket {

	private int weekCount;

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
		House house = player.getActiveHouse();
		long cost = HousingConfig.ENABLE_HOUSE_PAY ? house.getLand().getMaintenanceFee() * weekCount : 0;

		if (cost <= 0) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_F2P_CASH_HOUSE_FEE_FREE());
			return;
		}

		if (player.getInventory().getKinah() < cost) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_NOT_ENOUGH_MONEY());
			return;
		}

		Date nextPay = house.getNextPay() != null ? house.getNextPay() : MaintenanceTask.getInstance().getNextRun();
		for (int counter = 0; counter < weekCount; counter++)
			nextPay = MaintenanceTask.getInstance().getNextRunAfter(nextPay);

		long totalWeeksPaid = ChronoUnit.WEEKS.between(ServerTime.now().with(LocalTime.MIDNIGHT), ServerTime.atDate(nextPay));
		if (totalWeeksPaid > 4) // client cap
			return;

		player.getInventory().decreaseKinah(cost);
		house.setNextPay(nextPay);
		house.save();
		sendPacket(new SM_HOUSE_PAY_RENT(weekCount));
	}
}
