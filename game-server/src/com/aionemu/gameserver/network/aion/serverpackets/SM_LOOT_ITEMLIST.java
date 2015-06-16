package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Set;

import javolution.util.FastList;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.drop.Drop;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author alexa026, Avol, Corrected by Metos modified by ATracer, KID
 */
public class SM_LOOT_ITEMLIST extends AionServerPacket {

	private int targetObjectId;
	private FastList<DropItem> dropItems;

	public SM_LOOT_ITEMLIST(int targetObjectId, Set<DropItem> setItems, Player player) {
		this.targetObjectId = targetObjectId;
		this.dropItems = new FastList<DropItem>();
		if (setItems == null) {
			LoggerFactory.getLogger(SM_LOOT_ITEMLIST.class).warn("null Set<DropItem>, skip");
			return;
		}

		for (DropItem item : setItems) {
			if (item.canViewDropItem(player.getObjectId()))
				dropItems.add(item);
		}
	}

	/**
	 * {@inheritDoc} dc
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		writeD(targetObjectId);
		writeC(dropItems.size());

		for (DropItem dropItem : dropItems) {
			Drop drop = dropItem.getDropTemplate();
			writeC(dropItem.getIndex()); // index in droplist
			writeD(drop.getItemId());
			writeD((int) dropItem.getCount());
			writeC(dropItem.getOptionalSocket());
			writeC(0);
			writeC(0); // 3.5
			ItemTemplate template = drop.getItemTemplate();
			writeC(!template.getItemGroup().equals(ItemGroup.QUEST) && !template.isTradeable() ? 1 : 0);
		}
		FastList.recycle(dropItems);
	}

}