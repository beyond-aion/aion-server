package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * This blob sends info about conditioning.
 * 
 * @author -Nemesiss-
 * @modified Rolandas
 */
public class ConditioningInfoBlobEntry extends ItemBlobEntry {

	ConditioningInfoBlobEntry() {
		super(ItemBlobType.CONDITIONING_INFO);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {
		Item item = ownerItem;

		writeD(buf, item.getChargePoints());
	}

	@Override
	public int getSize() {
		return 4;
	}
}
