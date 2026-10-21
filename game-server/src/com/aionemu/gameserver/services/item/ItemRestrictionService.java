package com.aionemu.gameserver.services.item;

import com.aionemu.gameserver.configs.main.LegionConfig;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StorageObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.team.legion.LegionPermissionsMask;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.model.templates.pet.PetFunction;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer
 */
public class ItemRestrictionService {

	/**
	 * Check if item can be moved from storage by player
	 */
	public static boolean isItemRestrictedFrom(Player player, Item item, StorageType storageType) {
		if (!isStorageOpen(player, storageType))
			return true;
		switch (storageType) {
			case LEGION_WAREHOUSE:
				if (!LegionConfig.LEGION_WAREHOUSE || !player.isLegionMember() || !player.getLegionMember().hasRights(LegionPermissionsMask.WH_WITHDRAWAL)) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GUILD_WAREHOUSE_NO_RIGHT());
					return true;
				}
				if (!isLegionWarehouseUser(player))
					return true;
				break;
		}
		return false;
	}

	/**
	 * Check if item can be moved to storage by player
	 */
	public static boolean isItemRestrictedTo(Player player, Item item, StorageType storageType) {
		if (!isStorageOpen(player, storageType))
			return true;
		switch (storageType) {
			case REGULAR_WAREHOUSE:
				if (!item.isStorableInWarehouse()) {
					// You cannot store this in the warehouse.
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_WAREHOUSE_CANT_DEPOSIT_ITEM());
					return true;
				}
				break;
			case ACCOUNT_WAREHOUSE:
				if (!item.isStorableInAccWarehouse()) {
					// You cannot store this item in the account warehouse.
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_WAREHOUSE_CANT_ACCOUNT_DEPOSIT());
					return true;
				}
				break;
			case LEGION_WAREHOUSE:
				if (!item.isStorableInLegWarehouse() || !LegionConfig.LEGION_WAREHOUSE) {
					// You cannot store this item in the Legion warehouse.
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_WAREHOUSE_CANT_LEGION_DEPOSIT());
					return true;
				} else if (!player.isLegionMember() || (!player.getLegionMember().hasRights(LegionPermissionsMask.WH_DEPOSIT)
					&& !player.getLegionMember().hasRights(LegionPermissionsMask.WH_WITHDRAWAL))) {
					// You do not have the authority to use the Legion warehouse.
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GUILD_WAREHOUSE_NO_RIGHT());
					return true;
				}
				if (!isLegionWarehouseUser(player))
					return true;
				break;
		}

		return false;
	}

	/**
	 * @return False if the storage can only be used while its window is open and the player has no access to it at the moment.
	 */
	public static boolean isStorageOpen(Player player, StorageType storageType) {
		if (storageType == StorageType.REGULAR_WAREHOUSE || storageType == StorageType.ACCOUNT_WAREHOUSE || storageType == StorageType.LEGION_WAREHOUSE)
			return isAtStorageManager(player, storageType);
		if (storageType.getId() >= StorageType.PET_BAG_MIN && storageType.getId() <= StorageType.PET_BAG_MAX)
			return isPetBagOpen(player, storageType);
		if (storageType.getId() >= StorageType.HOUSE_WH_MIN && storageType.getId() <= StorageType.HOUSE_WH_MAX)
			return isCabinetOpen(player, storageType);
		return true;
	}

	private static boolean isCabinetOpen(Player player, StorageType storageType) {
		House house = player.getActiveHouse();
		if (house != null) {
			int warehouseId = storageType.getId() - StorageType.HOUSE_WH_MIN + 1;
			for (HouseObject<?> object : house.getRegistry().getSpawnedObjects()) {
				if (object instanceof StorageObject cabinet && cabinet.getObjectTemplate().getWarehouseId() == warehouseId && cabinet.isOccupiedBy(player)
					&& PositionUtil.isInTalkRange(player, cabinet))
					return true;
			}
		}
		AuditLogger.log(player, "tried to use " + storageType + " without using the cabinet");
		return false;
	}

	private static boolean isPetBagOpen(Player player, StorageType storageType) {
		PetFunction warehouse = player.getPet() == null ? null : player.getPet().getObjectTemplate().getWarehouseFunction();
		if (warehouse != null && warehouse.getPetBagStorageId() == storageType.getId())
			return true;
		AuditLogger.log(player, "tried to use " + storageType + " without the pet that carries it");
		return false;
	}

	/**
	 * @return True if the player opened the warehouse, legion warehouse or mailbox at an NPC and still stands in its talk range.
	 */
	public static boolean isAtStorageManager(Player player, StorageType storageType) {
		if (player.getStorageManager(storageType) instanceof Npc npc && PositionUtil.isInTalkRange(player, npc))
			return true;
		AuditLogger.log(player, "tried to use " + storageType + " away from a storage manager NPC");
		return false;
	}

	/**
	 * @return True if the player opened the legion warehouse at an NPC and holds it, so no other member can use it at the same time.
	 */
	public static boolean isLegionWarehouseUser(Player player) {
		if (player.isLegionMember() && player.getLegion().getLegionWarehouse().getCurrentUser() == player.getObjectId())
			return true;
		AuditLogger.log(player, "tried to use the legion warehouse without opening it");
		return false;
	}

	/** Check, whether the item can be removed */
	public static boolean canRemoveItem(Player player, Item item) {
		ItemTemplate it = item.getItemTemplate();
		if (it.getItemGroup().equals(ItemGroup.QUEST)) {
			// TODO: not removable, if quest status start and quest can not be abandoned
			// Waiting for quest data reparse
			return true;
		}
		return true;
	}

}
