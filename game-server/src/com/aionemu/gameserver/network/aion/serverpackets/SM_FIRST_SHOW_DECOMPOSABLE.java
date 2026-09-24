package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;

import com.aionemu.gameserver.model.templates.item.DecomposedItem;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author xTz
 */
public class SM_FIRST_SHOW_DECOMPOSABLE extends AionServerPacket {

	private Collection<DecomposedItem> itemsCollections;
	private int objectId;

	public SM_FIRST_SHOW_DECOMPOSABLE(int objectId, Collection<DecomposedItem> itemsCollections) {
		this.itemsCollections = itemsCollections;
		this.objectId = objectId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(objectId);
		writeD(0); // the object id is a 64 bit field, ours never fill the upper half
		writeC(itemsCollections.size());
		int index = 0;
		for (DecomposedItem item : itemsCollections) {
			writeC(index);
			writeD(item.getItemId());
			writeD(item.getCount());
			// properties of the reward the client shows in the window, all of them zero because a decomposed item is created plain
			writeC(0); // random bonus
			writeC(0); // enchant level
			writeC(0); // appraisal state
			writeC(1); // constant, the client stops reading the record after it
			index++;
		}
	}

}
