package com.aionemu.gameserver.services.item;

import java.util.Collection;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Equipment;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.taskmanager.tasks.ExpireTimerTask;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author KID
 */
public class ItemService {

	private static final Logger log = LoggerFactory.getLogger("ITEM_LOG");

	public static final ItemUpdatePredicate DEFAULT_UPDATE_PREDICATE = new ItemUpdatePredicate(ItemAddType.ITEM_COLLECT,
		ItemUpdateType.INC_ITEM_COLLECT);

	public static long addItem(Player player, int itemId, long count, boolean allowInventoryOverflow) {
		return addItem(player, itemId, count, null, allowInventoryOverflow, DEFAULT_UPDATE_PREDICATE);
	}

	public static long addItem(Player player, int itemId, long count) {
		return addItem(player, itemId, count, null, false, DEFAULT_UPDATE_PREDICATE);
	}

	public static long addItem(Player player, int itemId, long count, boolean allowInventoryOverflow, ItemUpdatePredicate predicate) {
		return addItem(player, itemId, count, null, allowInventoryOverflow, predicate);
	}

	/**
	 * Add new item based on all sourceItem values
	 */
	public static long addItem(Player player, Item sourceItem) {
		return addItem(player, sourceItem.getItemId(), sourceItem.getItemCount(), sourceItem, true, DEFAULT_UPDATE_PREDICATE);
	}

	/**
	 * Add new item based on all sourceItem values, but with different count
	 */
	public static long addItem(Player player, Item sourceItem, long count) {
		return addItem(player, sourceItem.getItemId(), count, sourceItem, false, DEFAULT_UPDATE_PREDICATE);
	}

	/**
	 * Add new item based on all sourceItem values, but with different count
	 */
	public static long addItem(Player player, Item sourceItem, long count, boolean allowInventoryOverflow, ItemUpdatePredicate predicate) {
		return addItem(player, sourceItem.getItemId(), count, sourceItem, allowInventoryOverflow, predicate);
	}

	/**
	 * Add new item based on sourceItem values
	 */
	private static long addItem(Player player, int itemId, long count, Item sourceItem, boolean allowInventoryOverflow, ItemUpdatePredicate predicate) {
		if (count <= 0)
			return 0;

		ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(itemId);
		Objects.requireNonNull(itemTemplate, "No item with id " + itemId);
		Objects.requireNonNull(predicate, "Predicate is not supplied");

		if (LoggingConfig.LOG_ITEM)
			log.info("Item: " + itemTemplate.getTemplateId() + " [" + itemTemplate.getName() + "] added to player " + player.getName() + " (count: " + count
				+ ") (type: " + predicate.getAddType() + ")");

		Storage inventory = player.getInventory();
		if (itemTemplate.isKinah()) {
			// quests do not add here
			inventory.increaseKinah(count);
			return 0;
		}

		if (itemTemplate.isStackable())
			count = addStackableItem(player, itemTemplate, count, allowInventoryOverflow, predicate);
		else
			count = addNonStackableItem(player, itemTemplate, count, sourceItem, allowInventoryOverflow, predicate);

		if (count > 0 && inventory.isFull(itemTemplate.getExtraInventoryId()))
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DICE_INVEN_ERROR());

		return count;
	}

	/**
	 * Add non-stackable item to inventory
	 */
	private static long addNonStackableItem(Player player, ItemTemplate itemTemplate, long count, Item sourceItem, boolean allowInventoryOverflow,
		ItemUpdatePredicate predicate) {
		Storage inventory = player.getInventory();
		while ((allowInventoryOverflow || !inventory.isFull(itemTemplate.getExtraInventoryId())) && count > 0) {
			Item newItem = ItemFactory.newItem(itemTemplate.getTemplateId());

			ExpireTimerTask.getInstance().registerExpirable(newItem, player);
			if (sourceItem != null) {
				copyItemInfo(sourceItem, newItem);
			}
			predicate.changeItem(newItem);
			inventory.add(newItem, predicate.getAddType());
			count--;
		}
		return count;
	}

	/**
	 * Copy some item values like item stones and enchant level, without any fusion item attributes
	 */
	public static void copyItemInfo(Item sourceItem, Item newItem) {
		newItem.setOptionalSockets(sourceItem.getOptionalSockets());
		newItem.setItemCreator(sourceItem.getItemCreator());
		if (sourceItem.hasManaStones()) {
			for (ManaStone manaStone : sourceItem.getItemStones())
				ItemSocketService.addManaStone(newItem, manaStone.getItemId(), false);
		}
		if (sourceItem.getGodStone() != null)
			newItem.addGodStone(sourceItem.getGodStone().getItemId(), sourceItem.getGodStone().getActivatedCount());
		newItem.setEnchantLevel(sourceItem.getEnchantLevel());
		newItem.setAmplified(sourceItem.isAmplified());
		newItem.setBuffSkill(sourceItem.getBuffSkill());
		newItem.setTempering(sourceItem.getTempering());
		newItem.setSoulBound(sourceItem.isSoulBound());
		newItem.setTuneCount(sourceItem.getTuneCount());
		newItem.setBonusStats(sourceItem.getBonusStatsId(), true);
		newItem.setIdianStone(sourceItem.getIdianStone());
		newItem.setItemColor(sourceItem.getItemColor());
		newItem.setEnchantBonus(sourceItem.getEnchantBonus());
		newItem.setItemSkinTemplate(sourceItem.getItemSkinTemplate());
	}

	/**
	 * Add stackable item to inventory
	 */
	private static long addStackableItem(Player player, ItemTemplate itemTemplate, long count, boolean allowInventoryOverflow,
		ItemUpdatePredicate predicate) {
		Collection<Item> items;
		// dirty & hacky check for arrows and shards...
		if (itemTemplate.getItemGroup() == ItemGroup.POWER_SHARDS) {
			Equipment equipment = player.getEquipment();
			items = equipment.getEquippedItemsByItemId(itemTemplate.getTemplateId());
			for (Item item : items) {
				if (count == 0) {
					break;
				}
				count = equipment.increaseEquippedItemCount(item, count);
			}
		}

		Storage inventory = player.getInventory();
		items = inventory.getItemsByItemId(itemTemplate.getTemplateId());
		for (Item item : items) {
			if (count == 0) {
				break;
			}
			count = inventory.increaseItemCount(item, count, predicate.getUpdateType(item, true));
		}

		while (count > 0 && (allowInventoryOverflow || !inventory.isFull(itemTemplate.getExtraInventoryId()))) {
			Item newItem = ItemFactory.newItem(itemTemplate.getTemplateId(), count);
			count -= newItem.getItemCount();
			inventory.add(newItem, predicate.getAddType());
		}
		return count;
	}

	public static class ItemUpdatePredicate {

		private final ItemUpdateType itemUpdateType;
		private final ItemAddType itemAddType;

		public ItemUpdatePredicate(ItemAddType itemAddType, ItemUpdateType itemUpdateType) {
			this.itemUpdateType = itemUpdateType;
			this.itemAddType = itemAddType;
		}

		public ItemUpdatePredicate() {
			this(ItemAddType.ITEM_COLLECT, ItemUpdateType.INC_ITEM_COLLECT);
		}

		public ItemUpdateType getUpdateType(Item item, boolean isIncrease) {
			if (item.getItemTemplate().isKinah())
				return ItemUpdateType.getKinahUpdateTypeFromAddType(itemAddType, isIncrease);
			return itemUpdateType;
		}

		public ItemAddType getAddType() {
			return itemAddType;
		}

		public boolean changeItem(Item item) {
			return true;
		}
	}

}
