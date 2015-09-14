package com.aionemu.gameserver.services.item;

import com.aionemu.gameserver.model.DescriptionId;
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
 * @author Sarynth modified by Wakizashi
 */
public class ItemRemodelService {

	/**
	 * @param player
	 * @param keepItemObjId
	 * @param extractItemObjId
	 */
	public static void remodelItem(Player player, int keepItemObjId, int extractItemObjId) {
		Storage inventory = player.getInventory();
		Item keepItem = inventory.getItemByObjId(keepItemObjId);
		Item extractItem = inventory.getItemByObjId(extractItemObjId);

		long remodelCost = PricesService.getPriceForService(1000, player.getRace());

		if (keepItem == null || extractItem == null) { // NPE check.
			return;
		}

		// Check Player Level
		if (player.getLevel() < 10) {

			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_PC_LEVEL_LIMIT);
			return;
		}

		if (keepItem.getItemTemplate().getUseLimits() != null && extractItem.getItemTemplate().getUseLimits() != null) {
			Gender keepItemGender = keepItem.getItemTemplate().getUseLimits().getGenderPermitted();
			Gender extractItemGender = extractItem.getItemTemplate().getUseLimits().getGenderPermitted();
			if (keepItemGender != null && extractItemGender != null) {
				if (keepItemGender != extractItemGender) {
					DescriptionId item1 = new DescriptionId(keepItem.getItemTemplate().getNameId());
					DescriptionId item2 = new DescriptionId(extractItem.getItemTemplate().getNameId());
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_CHANGE_SKIN_OPPOSITE_REQUIREMENT(item1, item2));
					return;
				}
			}
		}

		// Check Kinah
		if (player.getInventory().getKinah() < remodelCost) {
			PacketSendUtility.sendPacket(player,
				SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_NOT_ENOUGH_GOLD(new DescriptionId(keepItem.getItemTemplate().getNameId())));
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
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_SUCCEED(new DescriptionId(keepItem.getItemTemplate().getNameId())));
			return;
		}
		// Check that types match.
		ItemGroup keep = keepItem.getItemTemplate().getItemGroup();
		ItemGroup extract = extractItem.getItemSkinTemplate().getItemGroup();
		if ((keep != extract && !(extract.getItemSubType().equals(ItemSubType.CLOTHES) && keep.getSlots() == extract.getSlots()))
			|| keep.getItemSubType().equals(ItemSubType.CLOTHES)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_NOT_COMPATIBLE(new DescriptionId(keepItem.getItemTemplate()
				.getNameId()), new DescriptionId(extractItem.getItemSkinTemplate().getNameId())));
			return;
		}

		if (!keepItem.isRemodelable(player)) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300478, new DescriptionId(keepItem.getItemTemplate().getNameId())));
			return;
		}

		if (!extractItem.isRemodelable(player)) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300482, new DescriptionId(extractItem.getItemTemplate().getNameId())));
			return;
		}

		ItemTemplate skin = extractItem.getItemSkinTemplate();
		ItemActions actions = skin.getActions();
		if (extractItem.isSkinnedItem() && actions != null && actions.getRemodelAction() != null && actions.getRemodelAction().getExtractType() == 2) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300482, new DescriptionId(extractItem.getItemTemplate().getNameId())));
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
		PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300483, new DescriptionId(keepItem.getItemTemplate().getNameId())));
	}
}
