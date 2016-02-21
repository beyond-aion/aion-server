package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ResultedItem;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SECONDARY_SHOW_DECOMPOSABLE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
public class CM_SELECT_DECOMPOSABLE extends AionClientPacket {

	private int objectId;
	@SuppressWarnings("unused")
	private int unk;
	private int index;

	public CM_SELECT_DECOMPOSABLE(int opcode, AionConnection.State state, AionConnection.State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		objectId = readD();
		unk = readD();
		index = readC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player != null) {

			Item item = player.getInventory().getItemByObjId(objectId);
			if (item != null) {
				List<ResultedItem> selectableItems = DataManager.DECOMPOSABLE_ITEMS_DATA.getSelectableItems(item.getItemId());
				if (selectableItems == null) {
					return;
				}
				// filter By Class
				Iterator<ResultedItem> iter = selectableItems.iterator();
				while (iter.hasNext()) {
					ResultedItem i = iter.next();
					if (!i.getPlayerClass().equals(PlayerClass.ALL)) {
						if (!i.getPlayerClass().equals(player.getPlayerClass())) {
							iter.remove();
							continue;
						}
					}
					if (!i.getRace().equals(Race.PC_ALL)) {
						if (!i.getRace().equals(player.getRace())) {
							iter.remove();
						}
					}
				}
				if (index + 1 > selectableItems.size()) {
					return;
				}
				if (player.getInventory().isFull()) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DECOMPRESS_INVENTORY_IS_FULL);
					return;
				}
				PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), objectId, item.getItemId()));
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_USE_ITEM(new DescriptionId(item.getNameId())));
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_UNCOMPRESS_COMPRESSED_ITEM_SUCCEEDED(new DescriptionId(item.getNameId())));
				player.getInventory().decreaseByObjectId(objectId, 1);
				PacketSendUtility.sendPacket(player, new SM_SECONDARY_SHOW_DECOMPOSABLE(objectId, Collections.<ResultedItem> emptyList())); // to do
				ResultedItem selectedItem = selectableItems.get(index);
				ItemService.addItem(player, selectedItem.getItemId(), selectedItem.getResultCount());
			}
		}
	}

}
