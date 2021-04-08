package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * This blob entry is sent with ALL items. (unless partial blob is constructed, ie: sending equip slot only) It is the first and only block for
 * non-equipable items, and the last blob for EquipableItems
 * 
 * @author -Nemesiss-, Rolandas
 */
public class GeneralInfoBlobEntry extends ItemBlobEntry {

	GeneralInfoBlobEntry() {
		super(ItemBlobType.GENERAL_INFO);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {// TODO what with kinah?
		writeH(buf, ownerItem.getItemMask(owner));
		writeQ(buf, ownerItem.getItemCount());
		writeS(buf, ownerItem.getItemCreator());// Creator name
		writeC(buf, 0);
		writeD(buf, ownerItem.secondsUntilExpiration()); // Disappear time
		writeD(buf, 0);
		writeD(buf, ownerItem.getTemporaryExchangeTimeRemaining());
		writeH(buf, DataManager.ITEM_CLEAN_UP.hasAccountOrLegionWhStorabilityDisabled(ownerItem.getItemId()) ? 3 : 0); // TODO: Item Sealing - 1=sealed, 2=unsealing state, 3=special sealed(gm), 4=special unsealing(gm)
		writeD(buf, 0); // Remaining unsealing time
		writeH(buf, 18); // unk 4.7.5
	}

	@Override
	public int getSize() {
		return 29 + ownerItem.getItemCreator().length() * 2 + 4;
	}
}
