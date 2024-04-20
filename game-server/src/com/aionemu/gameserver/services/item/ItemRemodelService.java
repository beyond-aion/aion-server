package com.aionemu.gameserver.services.item;

import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.actions.ItemActions;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.model.templates.item.enums.ItemSubType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Sarynth, Wakizashi
 */
public class ItemRemodelService {

	public static void remodelItem(Player player, int keepItemObjId, int extractItemObjId) {
		Storage inventory = player.getInventory();
		Item keepItem = inventory.getItemByObjId(keepItemObjId);
		Item extractItem = inventory.getItemByObjId(extractItemObjId);

		long remodelCost = PricesService.getPriceForService(1000, player.getRace());

		if (keepItem == null || extractItem == null) // NPE check.
			return;

		// Check Player Level
		if (player.getLevel() < 10) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_PC_LEVEL_LIMIT());
			return;
		}

		if (keepItem.getItemTemplate().getUseLimits() != null && extractItem.getItemTemplate().getUseLimits() != null) {
			Gender keepItemGender = keepItem.getItemTemplate().getUseLimits().getGenderPermitted();
			Gender extractItemGender = extractItem.getItemTemplate().getUseLimits().getGenderPermitted();
			if (keepItemGender != null && extractItemGender != null) {
				if (keepItemGender != extractItemGender) {
					String item1 = keepItem.getItemTemplate().getL10n();
					String item2 = extractItem.getItemTemplate().getL10n();
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_CHANGE_SKIN_OPPOSITE_REQUIREMENT(item1, item2));
					return;
				}
			}
		}

		// Check Kinah
		if (player.getInventory().getKinah() < remodelCost) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_NOT_ENOUGH_GOLD(keepItem.getItemTemplate().getL10n()));
			return;
		}

		// Check for using "Pattern Reshaper" (168100000)
		if (extractItem.getItemTemplate().getTemplateId() == 168100000) {
			if (!keepItem.isSkinnedItem()) {
				PacketSendUtility.sendMessage(player, "That item does not have a remodeled skin to remove.");
				return;
			}
			// Remove Money
			if (!player.getInventory().tryDecreaseKinah(remodelCost))
				return;
			// Remove Pattern Reshaper
			player.getInventory().decreaseItemCount(extractItem, 1);

			// Revert item to ORIGINAL SKIN
			keepItem.setItemSkinTemplate(keepItem.getItemTemplate());

			// Remove dye color if item can not be dyed.
			if (!keepItem.getItemTemplate().isItemDyePermitted())
				keepItem.setItemColor(0);

			// Notify Player
			ItemPacketService.updateItemAfterInfoChange(player, keepItem);
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_SUCCEED(keepItem.getItemTemplate().getL10n()));
			return;
		}
		// Check that types match.
		ItemGroup keep = keepItem.getItemTemplate().getItemGroup();
		ItemGroup extract = extractItem.getItemSkinTemplate().getItemGroup();
		if ((keep != extract && !(extract.getItemSubType().equals(ItemSubType.CLOTHES)
			|| extract.getItemSubType() == ItemSubType.ALL_ARMOR && keep.getValidEquipmentSlots() == extract.getValidEquipmentSlots()))
			|| keep.getItemSubType().equals(ItemSubType.CLOTHES)) {
			PacketSendUtility.sendPacket(player,
				SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_NOT_COMPATIBLE(keepItem.getItemTemplate().getL10n(), extractItem.getItemSkinTemplate().getL10n()));
			return;
		}

		if (!keepItem.isRemodelable(player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_NOT_SKIN_CHANGABLE_ITEM(keepItem.getItemTemplate().getL10n()));
			return;
		}

		if (!extractItem.isRemodelable(player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_CAN_NOT_REMOVE_SKIN_ITEM(extractItem.getItemTemplate().getL10n()));
			return;
		}

		ItemTemplate skin = extractItem.getItemSkinTemplate();
		ItemActions actions = skin.getActions();
		if (extractItem.isSkinnedItem() && actions != null && actions.getRemodelAction() != null && actions.getRemodelAction().getExtractType() == 2) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_CAN_NOT_REMOVE_SKIN_ITEM(extractItem.getItemTemplate().getL10n()));
			return;
		}
		// -- SUCCESS --

		// Remove Money
		player.getInventory().decreaseKinah(remodelCost);

		// Remove Item
		player.getInventory().decreaseItemCount(extractItem, 1);

		// REMODEL ITEM
		keepItem.setItemSkinTemplate(skin);

		// Transfer Dye
		keepItem.setItemColor(extractItem.getItemColor());

		// Notify Player
		ItemPacketService.updateItemAfterInfoChange(player, keepItem);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_SUCCEED(keepItem.getItemTemplate().getL10n()));
	}
}
