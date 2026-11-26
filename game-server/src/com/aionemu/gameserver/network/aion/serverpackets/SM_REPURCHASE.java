package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob;
import com.aionemu.gameserver.services.RepurchaseService;

/**
 * @author xTz, KID
 */
public class SM_REPURCHASE extends AionServerPacket {

	private final int targetObjectId;
	private final List<RepurchaseItem> items;

	public SM_REPURCHASE(Player player, int npcId) {
		this.targetObjectId = npcId;
		this.items = new ArrayList<>();
		int size = 7 + 4 + 4 + 2; // header, targetObjectId, 1, count
		for (Item item : RepurchaseService.getInstance().getRepurchaseItems(player.getObjectId())) {
			String l10n = item.getL10n();
			ItemInfoBlob blob = ItemInfoBlob.getFullBlob(player, item);
			size += 4 + 4 + (l10n != null ? l10n.length() * 2 + 2 : 2) + blob.size() + 2 + 8; // objectId, id, l10n, blob (+ size field), price
			// The client has no limit on the number of items, but a packet larger than it can receive would not be sent at all. The items that don't
			// fit can be bought back once others have been.
			if (size > MAX_CLIENT_SUPPORTED_PACKET_SIZE)
				break;
			items.add(new RepurchaseItem(item.getObjectId(), item.getItemId(), l10n, blob, item.getRepurchasePrice()));
		}
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(targetObjectId);
		writeD(1); // unused by the client
		writeH(items.size());
		for (RepurchaseItem item : items) {
			writeD(item.objectId());
			writeD(item.id());
			writeS(item.l10n());
			item.blob().writeMe(getBuf());
			writeQ(item.price());
		}
	}

	private record RepurchaseItem(int objectId, int id, String l10n, ItemInfoBlob blob, long price) {}
}
