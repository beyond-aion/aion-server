package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;

import com.aionemu.gameserver.model.templates.item.ResultedItem;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author xTz
 */
public class SM_SECONDARY_SHOW_DECOMPOSABLE extends AionServerPacket {

	private Collection<ResultedItem> itemsCollections;
	private int objectId;

	public SM_SECONDARY_SHOW_DECOMPOSABLE(int objectId, Collection<ResultedItem> itemsCollections) {
		this.itemsCollections = itemsCollections;
		this.objectId = objectId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(objectId);
		writeD(0);
		writeC(itemsCollections.size());
		int index = 0;
		for (ResultedItem item : itemsCollections) {
			writeC(index);
			writeD(item.getItemId());
			writeD(item.getMinCount());
			writeC(0);
			writeC(0);
			writeC(0);
			writeC(1);
			index++;
		}
	}

}
