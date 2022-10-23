package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ResultedItem;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SECONDARY_SHOW_DECOMPOSABLE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
public class CM_SELECT_DECOMPOSABLE extends AionClientPacket {

	private int objectId;
	@SuppressWarnings("unused")
	private int unk;
	private int index;

	public CM_SELECT_DECOMPOSABLE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		objectId = readD();
		unk = readD();
		index = readUC();
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
				selectableItems.removeIf(i -> !i.isObtainableFor(player));
				if (index + 1 > selectableItems.size()) {
					return;
				}
				PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), objectId, item.getItemId()));
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_UNCOMPRESS_COMPRESSED_ITEM_SUCCEEDED(item.getL10n()));
				player.getInventory().decreaseByObjectId(objectId, 1);
				PacketSendUtility.sendPacket(player, new SM_SECONDARY_SHOW_DECOMPOSABLE(objectId, Collections.emptyList())); // TODO
				ResultedItem selectedItem = selectableItems.get(index);
				int count = Rnd.get(selectedItem.getMinCount(), selectedItem.getMaxCount());
				ItemService.addItem(player, selectedItem.getItemId(), count, true, new ItemUpdatePredicate(ItemAddType.DECOMPOSABLE, ItemUpdateType.INC_ITEM_COLLECT));
			}
		}
	}

}
