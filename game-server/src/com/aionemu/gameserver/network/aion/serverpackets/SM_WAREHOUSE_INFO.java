package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;
import java.util.Collections;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob;

/**
 * @author kosyachok
 */
public class SM_WAREHOUSE_INFO extends AionServerPacket {

	private int warehouseType;
	private Collection<Item> itemList;
	private boolean firstPacket;
	private int expandLvl;
	private Player player;

	public SM_WAREHOUSE_INFO(Collection<Item> items, int warehouseType, int expandLvl, boolean firstPacket, Player player) {
		this.warehouseType = warehouseType;
		this.expandLvl = expandLvl;
		this.firstPacket = firstPacket;
		if (items == null)
			this.itemList = Collections.emptyList();
		else
			this.itemList = items;
		this.player = player;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(warehouseType);
		writeC(firstPacket ? 1 : 0);
		writeC(expandLvl); // warehouse expand (0 - 9)
		if (warehouseType == StorageType.REGULAR_WAREHOUSE.getId() && itemList.size() > 0) {
			writeC(1);
			writeC(0); // unk, seen value 0x02
		} else {
			writeH(0);
		}
		writeH(itemList.size());
		for (Item item : itemList)
			writeItemInfo(item);
	}

	private void writeItemInfo(Item item) {
		ItemTemplate itemTemplate = item.getItemTemplate();

		writeD(item.getObjectId());
		writeD(itemTemplate.getTemplateId());
		writeC(0); // some item info (4 - weapon, 7 - armor, 8 - rings, 17 - bottles)
		writeS(itemTemplate.getL10n());

		ItemInfoBlob itemInfoBlob = ItemInfoBlob.getFullBlob(player, item);
		itemInfoBlob.writeMe(getBuf());

		writeH((int) (item.getEquipmentSlot() & 0xFFFF));
	}
}
