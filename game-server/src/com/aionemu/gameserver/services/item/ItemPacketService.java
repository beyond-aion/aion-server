package com.aionemu.gameserver.services.item;

import java.util.Collections;
import java.util.Objects;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * TODO: <br>
 * 0x01 0000 0001 increase count by merge<br>
 * 0x06 0000 0110 decrease count after split, equip<br>
 * 0x16 0001 0110 decrease count by use item<br>
 * 0x19 0001 1001 increase count by looting<br>
 * 0x1A 0001 1010 increase kinah by loot<br>
 * 0x1D 0001 1101 decrease kinah<br>
 * 0x32 0011 0010 increase kinah by quest<br>
 * 
 * @author ATracer
 */
public class ItemPacketService {

	public enum ItemUpdateType {
		EQUIP_UNEQUIP(-1, false), // internal usage only
		CHARGE(-2, false), // internal usage only
		POLISH_CHARGE(-3, false), // internal usage only
		STATS_CHANGE(0, true), // soul healer pay, manastone socketing, armor/weapons/arrows
		INC_ITEM_MERGE(0x01, true),
		INC_KINAH_MERGE(0x05, true),
		DEC_ITEM_SPLIT(0x06, true),
		DEC_ITEM_SPLIT_MOVE(0x0A, true), // move to other storage with split
		PUT(0x13, true), // from other storage
		DEC_ITEM_USE(0x16, true),
		DEC_STIGMA_USE(0x17, true),
		INC_ITEM_COLLECT(0x19, true),
		INC_KINAH_COLLECT(0x1A, true),
		INC_ITEM_BUY(0x1C, true), // buying from npc
		DEC_KINAH_BUY(0x1D, true),
		INC_KINAH_SELL(0x20, true),
		INC_PLAYER_EXCHANGE_GET_BACK(0x23, true),
		PUT_TO_EXCHANGE(0x25, true),
		INC_KINAH_QUEST(0x32, true),
		DEC_KINAH_LEARN(0x49, true), // craft skill learn
		DEC_KINAH_FLY(0x4B, true), // teleport or fly
		INC_CASH_ITEM(0x50, true), // event items, for exchange
		INC_ITEM_REPURCHASE(0x51, true),
		DEC_KINAH_CUBE(0x5A, true), // expand cube
		DEC_PET_FOOD(0x5E, true),
		INC_PASSPORT_ADD(0x8A, true);

		private final int mask;
		private final boolean sendable;

		ItemUpdateType(int mask, boolean sendable) {
			this.mask = mask;
			this.sendable = sendable;
		}

		public int getMask() {
			return mask;
		}

		public boolean isSendable() {
			return sendable;
		}

		public static ItemUpdateType getKinahUpdateTypeFromAddType(ItemAddType itemAddType, boolean isIncrease) {
			if (!isIncrease)
				return ItemUpdateType.DEC_KINAH_BUY;
			return switch (itemAddType) {
				case BUY -> ItemUpdateType.INC_KINAH_SELL;
				case ITEM_COLLECT -> ItemUpdateType.INC_KINAH_COLLECT;
				case QUEST_WORK_ITEM -> ItemUpdateType.INC_KINAH_QUEST;
				default -> ItemUpdateType.INC_KINAH_MERGE;
			};
		}
	}

	public static enum ItemAddType {
		SERVER_GENERATED(0x00), // Banquest Table, Extract Enchanment stones, Remodel, Armfusion
		PARTIAL_WITH_SLOT(0x07), // partial content of slot
		ALL_SLOT(0x13), // All warehouses/pets
		ITEM_COLLECT(0x19), // Drop, Gather
		BUY(0x1C),
		PLAYER_EXCHANGE_GET(0x21),
		PLAYER_EXCHANGE_GET_BACK(0x23),
		PLAYER_STORE(0x2B),
		CRAFTED_ITEM(0x2D),
		BROKER_BUY(0x2E),
		BROKER_RETURN(0x2F),
		QUEST_REWARD_ITEM(0x30),
		QUEST_WORK_ITEM(0x35),
		QUEST_WORK_ITEM2(0x36),
		MAIL(0x36),
		SURVEY(0x40),
		DECOMPOSABLE(0x50), // also Guestbloom
		REPURCHASE(0x51);

		private final int mask;

		ItemAddType(int mask) {
			this.mask = mask;
		}

		public int getMask() {
			return mask;
		}
	}

	public enum ItemDeleteType {
		DEFAULT(0),
		SPLIT(0x04),
		MOVE(0x14),
		DISCARD(0x15),
		USE(0x17),
		SELL(0x1F),
		QUEST_COMPLETE(0x31),
		QUEST_START(0x34),
		DECOMPOSE(0x66),
		REGISTER(0x78),
		PUT_TO_EXCHANGE(0x26);

		private final int mask;

		ItemDeleteType(int mask) {
			this.mask = mask;
		}

		public int getMask() {
			return mask;
		}

		public static ItemDeleteType fromUpdateType(ItemUpdateType updateType) {
			return switch (updateType) {
				case DEC_ITEM_SPLIT -> SPLIT;
				case DEC_ITEM_USE -> USE;
				case DEC_ITEM_SPLIT_MOVE -> MOVE;
				default -> DEFAULT;
			};
		}

		public static ItemDeleteType fromQuestStatus(QuestStatus questStatus) {
			return switch (questStatus) {
				case START -> QUEST_START;
				case COMPLETE -> QUEST_COMPLETE;
				default -> DEFAULT;
			};
		}
	}

	public static void updateItemAfterInfoChange(Player player, Item item) {
		PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, item));
	}

	public static void updateItemAfterInfoChange(Player player, Item item, ItemUpdateType updateType) {
		PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, item, updateType));
	}

	public static void updateItemAfterEquip(Player player, Item item) {
		PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, item, ItemUpdateType.EQUIP_UNEQUIP));
	}

	public static void sendItemPacket(Player player, StorageType storageType, Item item, ItemUpdateType updateType) {
		if (item.getItemCount() <= 0 && !item.getItemTemplate().isKinah()) {
			sendItemDeletePacket(player, storageType, item, ItemDeleteType.fromUpdateType(updateType));
		} else {
			sendItemUpdatePacket(player, storageType, item, updateType);
		}
	}

	/**
	 * Item will be deleted from UI slot
	 */
	public static void sendItemDeletePacket(Player player, StorageType storageType, Item item, ItemDeleteType deleteType) {
		if (Objects.requireNonNull(storageType) == StorageType.CUBE) {
			PacketSendUtility.sendPacket(player, new SM_DELETE_ITEM(item.getObjectId(), deleteType));
		} else {
			PacketSendUtility.sendPacket(player, new SM_DELETE_WAREHOUSE_ITEM(storageType.getId(), item.getObjectId(), deleteType));
		}
		PacketSendUtility.sendPacket(player, SM_CUBE_UPDATE.cubeSize(storageType, player));
	}

	/**
	 * Item will be updated in UI slot (stacked items)
	 */
	@SuppressWarnings("fallthrough")
	public static void sendItemUpdatePacket(Player player, StorageType storageType, Item item, ItemUpdateType updateType) {
		switch (storageType) {
			case CUBE:
				PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, item, updateType));
				break;
			case LEGION_WAREHOUSE:
				if (item.getItemTemplate().isKinah()) {
					PacketSendUtility.sendPacket(player, new SM_LEGION_EDIT(0x04, player.getLegion()));
					break;
				}
			default:
				PacketSendUtility.sendPacket(player, new SM_WAREHOUSE_UPDATE_ITEM(player, item, storageType.getId(), updateType));
		}
	}

	public static void sendStorageUpdatePacket(Player player, StorageType storageType, Item item) {
		sendStorageUpdatePacket(player, storageType, item, ItemAddType.ITEM_COLLECT);
	}

	/**
	 * New item will be displayed in storage
	 */
	@SuppressWarnings("fallthrough")
	public static void sendStorageUpdatePacket(Player player, StorageType storageType, Item item, ItemAddType addType) {
		switch (storageType) {
			case CUBE:
				PacketSendUtility.sendPacket(player, new SM_INVENTORY_ADD_ITEM(Collections.singletonList(item), player, addType));
				break;
			case LEGION_WAREHOUSE:
				if (item.getItemTemplate().isKinah()) {
					PacketSendUtility.sendPacket(player, new SM_LEGION_EDIT(0x04, player.getLegion()));
					break;
				}
			default:
				PacketSendUtility.sendPacket(player, new SM_WAREHOUSE_ADD_ITEM(item, storageType.getId(), player, addType));
		}
		PacketSendUtility.sendPacket(player, SM_CUBE_UPDATE.cubeSize(storageType, player));
	}

	public static void sendItemUnlockPacket(Player player, Item item) {
		StorageType storageType = StorageType.getStorageTypeById(item.getItemLocation());
		if (storageType != null)
			sendStorageUpdatePacket(player, storageType, item, ItemAddType.ALL_SLOT);
	}
}
