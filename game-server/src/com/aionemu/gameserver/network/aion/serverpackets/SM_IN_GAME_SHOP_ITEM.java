package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.ingameshop.IGItem;
import com.aionemu.gameserver.model.ingameshop.InGameShopEn;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author xTz, KID
 */
public class SM_IN_GAME_SHOP_ITEM extends AionServerPacket {

	private IGItem item;

	public SM_IN_GAME_SHOP_ITEM(Player player, int objectItem) {
		item = InGameShopEn.getInstance().getIGItem(objectItem);
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(item.getObjectId()); // nrItem
		writeQ(item.getItemPrice()); // price
		writeH(0);
		writeD(item.getItemId()); // itemId
		writeQ(item.getItemCount()); // itemCount
		writeD(0); // unk
		writeD(item.getGift()); // gift 0, 1
		writeD(item.getItemType()); // item type 0, 1, 2
		writeD(0); // unk
		writeC(0); // unk
		writeH(0); // unk
		writeS(item.getTitleDescription());
		writeS(item.getItemDescription());
	}

}
