package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Map;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PrivateStore;
import com.aionemu.gameserver.model.trade.TradePSItem;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob;

/**
 * @author Simple
 */
public class SM_PRIVATE_STORE extends AionServerPacket {

	private Player player;
	private PrivateStore store;

	public SM_PRIVATE_STORE(PrivateStore store, Player player) {
		this.player = player;
		this.store = store;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		if (store != null) {
			Player seller = store.getOwner();
			Map<Integer, TradePSItem> soldItems = store.getSoldItems();

			writeD(seller.getObjectId());
			writeH(soldItems.size());
			for (TradePSItem tradeItem : soldItems.values()) {
				writeD(tradeItem.getItemObjId());
				writeD(tradeItem.getItemId());
				writeH((int) tradeItem.getCount());
				writeQ(tradeItem.getPrice());
				ItemInfoBlob.getFullBlob(player, seller.getInventory().getItemByObjId(tradeItem.getItemObjId())).writeMe(getBuf());
			}
		}
	}
}
