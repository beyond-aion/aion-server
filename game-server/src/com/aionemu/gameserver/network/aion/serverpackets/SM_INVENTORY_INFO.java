package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob;

/**
 * In this packet Server is sending Inventory Info
 * 
 * @author -Nemesiss-, alexa026, Avol ;d, ATracer, Rolandas, Artur
 */
public class SM_INVENTORY_INFO extends AionServerPacket {

	private boolean isFirstPacket;
	private List<Item> items;
	private Player player;

	public SM_INVENTORY_INFO(boolean isFirstPacket, List<Item> items, Player player) {
		// this should prevent client crashes but need to discover when item is null
		items.removeAll(Collections.singletonList(null));
		this.isFirstPacket = isFirstPacket;
		this.items = items;
		this.player = player;
	}

	@Override
	protected void writeImpl(AionConnection con) {

		// something wrong with cube part.
		writeC(isFirstPacket ? 1 : 0);
		writeC(player.getNpcExpands()); // cube size from npc (so max 5 for now)
		writeC(player.getQuestExpands()); // cube size from quest (so max 2 for now)
		writeC(player.getItemExpands()); // count of ticket expands
		writeH(items.size()); // number of entries
		for (Item item : items)
			writeItemInfo(item);
	}

	private void writeItemInfo(Item item) {
		ItemTemplate itemTemplate = item.getItemTemplate();

		writeD(item.getObjectId());
		writeD(itemTemplate.getTemplateId());
		writeS(itemTemplate.getL10n());

		ItemInfoBlob itemInfoBlob = ItemInfoBlob.getFullBlob(player, item);
		itemInfoBlob.writeMe(getBuf());

		// invisible -1, visible is a slot
		writeH((int) (item.getEquipmentSlot() & 0xFFFF));

		// probably a right to equip the item, related to passive skill learn
		writeC(itemTemplate.isCloth() ? 1 : 0);
	}
}
