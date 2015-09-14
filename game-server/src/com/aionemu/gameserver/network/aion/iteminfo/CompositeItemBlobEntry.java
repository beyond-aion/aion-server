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
 * @author -Nemesiss-
 * @modified Rolandas
 */
public class CompositeItemBlobEntry extends ItemBlobEntry {

	CompositeItemBlobEntry() {
		super(ItemBlobType.COMPOSITE_ITEM);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {
		Item item = ownerItem;

		writeD(buf, item.getFusionedItemId());
		writeFusionStones(buf);
		// TODO: verify it
		writeH(buf, item.hasOptionalFusionSocket() ? item.getOptionalFusionSocket() : 0x00);
	}

	private void writeFusionStones(ByteBuffer buf) {
		Item item = ownerItem;

		if (item.hasFusionStones()) {
			Set<ManaStone> itemStones = item.getFusionStones();
			HashMap<Integer, ManaStone> stonesBySlot = new HashMap<>();
			for (ManaStone itemStone : itemStones) {
				stonesBySlot.put(itemStone.getSlot(), itemStone);
			}
			for (int i = 0; i < Item.MAX_BASIC_STONES; i++) {
				ManaStone stone = stonesBySlot.get(i);
				if (stone == null)
					writeD(buf, 0);
				else
					writeD(buf, stone.getItemId());
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
