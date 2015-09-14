package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * This blob entry is sent with ALL items. (unless partial blob is constructed, ie: sending equip slot only) It is the first and only block for
 * non-equipable items, and the last blob for EquipableItems
 * 
 * @author -Nemesiss-
 * @modified Rolandas
 */
public class GeneralInfoBlobEntry extends ItemBlobEntry {

	GeneralInfoBlobEntry() {
		super(ItemBlobType.GENERAL_INFO);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {// TODO what with kinah?
		Item item = ownerItem;
		writeH(buf, item.getItemMask(owner));
		writeQ(buf, item.getItemCount());
		writeS(buf, item.getItemCreator());// Creator name
		writeC(buf, 0);
		writeD(buf, item.getExpireTimeRemaining()); // Disappears time
		writeD(buf, 0);
		writeD(buf, item.getTemporaryExchangeTimeRemaining());
		writeH(buf, 0);// TODO sealed
		writeD(buf, 0);
		writeH(buf, 18);// unk 4.7.5
	}

	@Override
	public int getSize() {
		return 29 + ownerItem.getItemCreator().length() * 2 + 4;
	}
}
