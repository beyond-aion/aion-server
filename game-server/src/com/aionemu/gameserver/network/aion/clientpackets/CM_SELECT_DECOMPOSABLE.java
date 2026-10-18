package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemUseAnimation;
import com.aionemu.gameserver.model.templates.item.DecomposableItemInfo;
import com.aionemu.gameserver.model.templates.item.DecomposableSet;
import com.aionemu.gameserver.model.templates.item.DecomposedItem;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SECONDARY_SHOW_DECOMPOSABLE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.restrictions.PlayerRestrictions;
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
	private int index;

	public CM_SELECT_DECOMPOSABLE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		objectId = readD();
		readD(); // the object id is a 64 bit field, ours never fill the upper half
		index = readUC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null)
			return;
		Item item = player.getInventory().getItemByObjId(objectId);
		if (item == null)
			return;
		DecomposableItemInfo info = DataManager.DECOMPOSABLE_ITEMS_DATA.getInfoByItemId(item.getItemId());
		// nothing ties the pick to an opened window, so it has to pass the same checks as using the item
		if (info == null || !info.isSelectable() || !PlayerRestrictions.canUseItem(player, item))
			return;
		DecomposableSet set = info.getSelectableSet(player);
		if (set == null || index >= set.getItems().size())
			return;
		DecomposedItem selectedItem = set.getItems().get(index);
		if (player.getInventory().isFull(DataManager.ITEM_DATA.getItemTemplate(selectedItem.getItemId()).getExtraInventoryId())) {
			refuse(player, item);
			return;
		}
		PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), objectId, item.getItemId()));
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_USE_ITEM(item.getL10n()));
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_UNCOMPRESS_COMPRESSED_ITEM_SUCCEEDED(item.getL10n()));
		if (!player.getInventory().decreaseByObjectId(objectId, 1)) {
			PacketSendUtility.sendPacket(player, new SM_SECONDARY_SHOW_DECOMPOSABLE(objectId, SM_SECONDARY_SHOW_DECOMPOSABLE.NOT_GRANTED));
			return;
		}
		PacketSendUtility.sendPacket(player, new SM_SECONDARY_SHOW_DECOMPOSABLE(objectId, SM_SECONDARY_SHOW_DECOMPOSABLE.GRANTED));
		ItemService.addItem(player, selectedItem.getItemId(), selectedItem.getCount(), true,
			new ItemUpdatePredicate(ItemAddType.DECOMPOSABLE, ItemUpdateType.INC_ITEM_COLLECT));
		player.startCooldown(item);
	}

	private void refuse(Player player, Item item) {
		PacketSendUtility.broadcastPacketAndReceive(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), objectId, item.getItemId(), 0, ItemUseAnimation.USE_FAIL));
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANT_USE_ITEM(item.getL10n()));
		PacketSendUtility.sendPacket(player, new SM_SECONDARY_SHOW_DECOMPOSABLE(objectId, SM_SECONDARY_SHOW_DECOMPOSABLE.NOT_GRANTED));
	}

}
