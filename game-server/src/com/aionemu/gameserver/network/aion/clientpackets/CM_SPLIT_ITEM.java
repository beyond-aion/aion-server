package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemSplitService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author kosyak
 */
public class CM_SPLIT_ITEM extends AionClientPacket {

	int sourceItemObjId;
	byte sourceStorageType;
	long itemAmount;
	int destinationItemObjId;
	byte destinationStorageType;
	short slotNum;

	public CM_SPLIT_ITEM(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		sourceItemObjId = readD();
		itemAmount = readD();

		readB(4); // Nothing

		sourceStorageType = readSC();
		destinationItemObjId = readD();
		destinationStorageType = readSC();
		slotNum = readSH();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		
		if(player.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_MOVE_ITEMS) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(player, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}
		
		ItemSplitService.splitItem(player, sourceItemObjId, destinationItemObjId, itemAmount, slotNum, sourceStorageType,
			destinationStorageType);
	}
}
