package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob;
import com.aionemu.gameserver.services.RepurchaseService;

/**
 * @author xTz, KID
 */
public class SM_REPURCHASE extends AionServerPacket {

	private Player player;
	private final int targetObjectId;
	private final Collection<Item> items;

	public SM_REPURCHASE(Player player, int npcId) {
		this.player = player;
		this.targetObjectId = npcId;
		items = RepurchaseService.getInstance().getRepurchaseItems(player.getObjectId());
	}

	@Override
	protected void writeImpl(AionConnection con) {
		int packetStart = getBuf().writerIndex() - 7; // size, opcode, static code and flipped opcode
		writeD(targetObjectId);
		writeD(1);
		int countIndex = getBuf().writerIndex();
		writeH(0);
		int count = 0;
		for (Item item : items) {
			int itemStart = getBuf().writerIndex();
			ItemTemplate itemTemplate = item.getItemTemplate();

			writeD(item.getObjectId());
			writeD(itemTemplate.getTemplateId());
			writeS(itemTemplate.getL10n());

			ItemInfoBlob itemInfoBlob = ItemInfoBlob.getFullBlob(player, item);
			itemInfoBlob.writeMe(getBuf());

			writeQ(item.getRepurchasePrice());
			// The client has no limit on the number of items, but a packet larger than it can receive would not be sent at all. The items that don't
			// fit can be bought back once others have been.
			if (getBuf().writerIndex() - packetStart > MAX_CLIENT_SUPPORTED_PACKET_SIZE) {
				getBuf().writerIndex(itemStart);
				break;
			}
			count++;
		}
		getBuf().setShortLE(countIndex, count);
	}
}
