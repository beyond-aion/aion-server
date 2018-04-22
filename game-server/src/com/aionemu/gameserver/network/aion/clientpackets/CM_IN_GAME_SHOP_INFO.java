package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.configs.main.InGameShopConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.ingameshop.InGameShopEn;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_IN_GAME_SHOP_CATEGORY_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_IN_GAME_SHOP_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_IN_GAME_SHOP_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TOLL_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz, KID
 */
public class CM_IN_GAME_SHOP_INFO extends AionClientPacket {

	private int actionId;
	private int categoryId;
	private int listInCategory;
	private String senderName;
	private String senderMessage;

	public CM_IN_GAME_SHOP_INFO(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		actionId = readUC();
		categoryId = readD();
		listInCategory = readD();
		senderName = readS();
		senderMessage = readS();
	}

	@Override
	protected void runImpl() {
		if (InGameShopConfig.ENABLE_IN_GAME_SHOP) {
			Player player = getConnection().getActivePlayer();

			switch (actionId) {
				case 0x01: // item info
					PacketSendUtility.sendPacket(player, new SM_IN_GAME_SHOP_ITEM(player, categoryId));
					break;
				case 0x02: // change category
					PacketSendUtility.sendPacket(player, new SM_IN_GAME_SHOP_CATEGORY_LIST(2, categoryId));
					player.inGameShop.setCategory((byte) categoryId);
					break;
				case 0x04: // category list
					PacketSendUtility.sendPacket(player, new SM_IN_GAME_SHOP_CATEGORY_LIST(0, categoryId));
					break;
				case 0x08: // showcat
					if (categoryId > 1)
						player.inGameShop.setSubCategory((byte) categoryId);

					PacketSendUtility.sendPacket(player, new SM_IN_GAME_SHOP_LIST(player, listInCategory, 1));
					PacketSendUtility.sendPacket(player, new SM_IN_GAME_SHOP_LIST(player, listInCategory, 0));
					break;
				case 0x10: // balance
					PacketSendUtility.sendPacket(player, new SM_TOLL_INFO(player.getClientConnection().getAccount().getToll()));
					break;
				case 0x20: // buy
					InGameShopEn.getInstance().buyItemRequest(player, categoryId);
					break;
				case 0x40: // gift
					InGameShopEn.getInstance().giftItemRequest(player, senderName, senderMessage, categoryId);
					break;
			}
		}
	}
}
