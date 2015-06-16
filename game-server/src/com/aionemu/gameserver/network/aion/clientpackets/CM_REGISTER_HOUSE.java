package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.configs.main.HousingConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.HouseStatus;
import com.aionemu.gameserver.model.templates.housing.HouseType;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_OWNER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.HousingBidService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rolandas
 */
public class CM_REGISTER_HOUSE extends AionClientPacket {

	long bidKinah;
	long unk1;

	public CM_REGISTER_HOUSE(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		bidKinah = readQ();
		unk1 = readQ(); // 100000
	}

	@Override
	protected void runImpl() {
		if (!HousingConfig.ENABLE_HOUSE_AUCTIONS)
			return;

		Player player = getConnection().getActivePlayer();
		
		if(player.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_MANAGE_HOUSE) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(player, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}
		
		House house = player.getActiveHouse();
		if (house == null || house.getHouseType() == HouseType.STUDIO)
			return; // should not happen
		
		if (house.getStatus() == HouseStatus.SELL_WAIT) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_AUCTION_FAIL_ALREADY_REGISTED);
			return;
		}

		if (!HousingBidService.getInstance().isRegisteringAllowed()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_AUCTION_TIMEOUT);
			return;
		}

		if (!house.isFeePaid() && HousingConfig.ENABLE_HOUSE_PAY) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_AUCTION_OVERDUE);
			return;
		}

		long fee = (long) (bidKinah * 0.3f);

		if (player.getInventory().getKinah() < fee) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_NOT_ENOUGH_MONEY);
			return;
		}
		player.getInventory().decreaseKinah(fee);
		HousingBidService.getInstance().addHouseToAuction(house, bidKinah);

		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_AUCTION_MY_HOUSE(house.getAddress().getId()));
		house.getController().updateAppearance();

		PacketSendUtility.sendPacket(player, new SM_HOUSE_OWNER_INFO(player));
	}

}
