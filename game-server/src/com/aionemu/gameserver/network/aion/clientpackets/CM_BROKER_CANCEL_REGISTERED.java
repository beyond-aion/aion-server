package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.BrokerService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author kosyachok
 */
public class CM_BROKER_CANCEL_REGISTERED extends AionClientPacket {

	@SuppressWarnings("unused")
	private int npcId;
	private int brokerItemId;

	public CM_BROKER_CANCEL_REGISTERED(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		npcId = readD();
		brokerItemId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (player.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_BROKER) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(player, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}

		BrokerService.getInstance().cancelRegisteredItem(player, brokerItemId);
	}
}
