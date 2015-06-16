package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.LinkedHashMap;

import com.aionemu.gameserver.model.gameobjects.Item;
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
	/** Private store Information **/
	private PrivateStore store;

	public SM_PRIVATE_STORE(PrivateStore store, Player player) {
		this.player = player;
		this.store = store;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		if (store != null) {
			Player storePlayer = store.getOwner();
			LinkedHashMap<Integer, TradePSItem> soldItems = store.getSoldItems();

			writeD(storePlayer.getObjectId());
			writeH(soldItems.size());
			for (Integer itemObjId : soldItems.keySet()) {
				Item item = storePlayer.getInventory().getItemByObjId(itemObjId);
				TradePSItem tradeItem = store.getTradeItemByObjId(itemObjId);
				long price = tradeItem.getPrice();
				writeD(itemObjId);
				writeD(item.getItemTemplate().getTemplateId());
				writeH((int) tradeItem.getCount());
				writeQ(price);

				ItemInfoBlob itemInfoBlob = ItemInfoBlob.getFullBlob(player, item);
				itemInfoBlob.writeMe(getBuf());
			}
		}
	}
}
