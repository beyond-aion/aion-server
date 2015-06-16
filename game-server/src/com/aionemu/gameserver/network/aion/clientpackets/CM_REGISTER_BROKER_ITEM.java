package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.BrokerService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author kosyak
 */
public class CM_REGISTER_BROKER_ITEM extends AionClientPacket {

	@SuppressWarnings("unused")
	private int brokerId;
	private int itemUniqueId;
	private long price;
	private long itemCount;
	private boolean splittingAvailable;

	public CM_REGISTER_BROKER_ITEM(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		this.brokerId = readD();
		this.itemUniqueId = readD();
		this.price = readQ();
		this.itemCount = readQ();
		this.splittingAvailable = readC() == 1 ? true : false;
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (player.isTrading() || price < 1 || itemCount < 1)
			return;
		
		if(player.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_BROKER) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(player, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}

		BrokerService.getInstance().registerItem(player, itemUniqueId, itemCount, price, splittingAvailable);
	}
}
