package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * This blob is sending info about the item that were fused with current item.
 * 
 * @author -Nemesiss-, Rolandas
 */
public class CompositeItemBlobEntry extends ItemBlobEntry {

	CompositeItemBlobEntry() {
		super(ItemBlobType.COMPOSITE_ITEM);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {
		writeD(buf, ownerItem.getFusionedItemId());
		writeFusionStones(buf);
		writeC(buf, ownerItem.getFusionedItemOptionalSockets()); // additional manastone sockets
		writeC(buf, ownerItem.getFusionedItemBonusStatsId());
	}

	private void writeFusionStones(ByteBuffer buf) {
		if (ownerItem.hasFusionStones()) {
			Set<ManaStone> itemStones = ownerItem.getFusionStones();
			HashMap<Integer, ManaStone> stonesBySlot = new HashMap<>();
			for (ManaStone itemStone : itemStones) {
				stonesBySlot.put(itemStone.getSlot(), itemStone);
			}
			for (int i = 0; i < Item.MAX_BASIC_STONES; i++) {
				ManaStone stone = stonesBySlot.get(i);
				writeD(buf, stone == null ? 0 : stone.getItemId());
			}
		} else {
			skip(buf, Item.MAX_BASIC_STONES * 4);
		}
	}

	@Override
	public int getSize() {
		return Item.MAX_BASIC_STONES * 4 + 6;
	}
}
