package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.HouseType;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RECEIVE_BIDS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.HousingBidService;

/**
 * @author Rolandas
 */
public class CM_REGISTER_HOUSE extends AionClientPacket {

	private long bidKinah;

	public CM_REGISTER_HOUSE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		bidKinah = readQ();
		readQ(); // always 100000
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (!HousingBidService.getInstance().isRegisteringAllowed()) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_AUCTION_TIMEOUT());
			return;
		}

		House house = player.getActiveHouse();
		if (house == null || house.getHouseType() == HouseType.STUDIO)
			return; // should not happen

		if (house.getBids() != null) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_AUCTION_FAIL_ALREADY_REGISTED());
			return;
		}

		if (!house.isFeePaid() && HousingConfig.ENABLE_HOUSE_PAY) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_AUCTION_OVERDUE());
			return;
		}

		long fee = (long) (bidKinah * HousingConfig.AUCTION_REGISTRATION_FEE_PERCENT);

		if (!player.getInventory().tryDecreaseKinah(fee)) {
			// client has it's own validation, so we only get here if AUCTION_REGISTRATION_FEE_PERCENT is higher than the default in the client (30%)
			sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_KINA(fee));
			return;
		}
		if (HousingBidService.getInstance().auction(house, bidKinah)) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_AUCTION_MY_HOUSE(house.getAddress().getId()));
			sendPacket(new SM_RECEIVE_BIDS(0));
		} else {
			sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_AUCTION_TIMEOUT());
			player.getInventory().increaseKinah(fee);
		}
	}

}
