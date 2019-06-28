package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.ItemStorage;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;

/**
 * @author ATracer
 */
public class SM_INVENTORY_ADD_ITEM extends AionServerPacket {

	private final List<Item> items;
	private Player player;
	private ItemAddType addType;

	public SM_INVENTORY_ADD_ITEM(List<Item> items, Player player, ItemAddType addType) {
		this.player = player;
		this.items = items;
		this.addType = addType;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		// TODO: Rework it, who knows where it could be bugged else.
		int mask = addType.getMask();
		if (addType == ItemAddType.ITEM_COLLECT) {
			// TODO: if size != 1, then it's buy item, should not specify any slot in other places then !!!
			if (items.size() == 1 && items.get(0).getEquipmentSlot() != ItemStorage.FIRST_AVAILABLE_SLOT)
				mask = ItemAddType.PARTIAL_WITH_SLOT.getMask();
		}
		writeH(mask); //
		writeH(items.size()); // number of entries
		for (Item item : items)
			writeItemInfo(item);
	}

	private void writeItemInfo(Item item) {
		ItemTemplate itemTemplate = item.getItemTemplate();

		writeD(item.getObjectId());
		writeD(itemTemplate.getTemplateId());
		writeS(itemTemplate.getL10n());

		ItemInfoBlob.getFullBlob(player, item).writeMe(getBuf());

		writeH((int) (item.getEquipmentSlot() & 0xFFFF));
		writeC(item.getItemTemplate().isCloth() ? 1 : 0);
	}
}
