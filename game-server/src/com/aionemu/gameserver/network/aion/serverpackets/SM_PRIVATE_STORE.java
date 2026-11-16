package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.ArrayList;
import java.util.List;

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

	private final Player player;
	private final boolean hasStore;
	private final int sellerObjectId;
	// Snapshot: the seller and the buyers change the offered items on their own threads while this packet waits for an I/O thread
	private final List<TradePSItem> soldItems = new ArrayList<>();
	private final List<Item> soldItemInstances = new ArrayList<>();

	public SM_PRIVATE_STORE(PrivateStore store, Player player) {
		this.player = player;
		hasStore = store != null;
		if (!hasStore) {
			sellerObjectId = 0;
			return;
		}
		Player seller = store.getOwner();
		sellerObjectId = seller.getObjectId();
		for (TradePSItem tradeItem : store.getSoldItems().values()) {
			Item item = seller.getInventory().getItemByObjId(tradeItem.getItemObjId());
			if (item != null) { // sold in the meantime, the item of this entry is gone
				soldItems.add(tradeItem);
				soldItemInstances.add(item);
			}
		}
	}

	@Override
	protected void writeImpl(AionConnection con) {
		if (!hasStore)
			return;
		writeD(sellerObjectId);
		writeH(soldItems.size());
		for (int i = 0; i < soldItems.size(); i++) {
			TradePSItem tradeItem = soldItems.get(i);
			writeD(tradeItem.getItemObjId());
			writeD(tradeItem.getItemId());
			writeH((int) tradeItem.getCount());
			writeQ(tradeItem.getPrice());
			ItemInfoBlob.getFullBlob(player, soldItemInstances.get(i)).writeMe(getBuf());
		}
	}
}
