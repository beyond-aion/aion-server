package com.aionemu.gameserver.network.aion.iteminfo;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.network.PacketWriteHelper;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

import io.netty.buffer.ByteBuf;

/**
 * ItemInfo blob entry (contains detailed item info). Client does have blob tree as implemented, it contains sequence of blobs. Just blame Nemesiss
 * for deep recursion to get the right size [RR] :P
 * 
 * @author -Nemesiss-, Rolandas
 */
public abstract class ItemBlobEntry extends PacketWriteHelper {

	private final ItemBlobType type;
	Player owner;
	Item ownerItem;
	IStatFunction modifier;

	ItemBlobEntry(ItemBlobType type) {
		this.type = type;
	}

	void setOwner(Player owner, Item item, IStatFunction modifier) {
		this.owner = owner;
		this.ownerItem = item;
		this.modifier = modifier;
	}

	@Override
	protected void writeMe(ByteBuf buf) {
		writeC(buf, type.getEntryId());
		writeThisBlob(buf);
	}

	public abstract void writeThisBlob(ByteBuf buf);

	public abstract int getSize();

}
