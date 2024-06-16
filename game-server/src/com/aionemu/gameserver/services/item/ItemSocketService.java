package com.aionemu.gameserver.services.item;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.aionemu.gameserver.controllers.observer.StartMovingListener;
import com.aionemu.gameserver.dao.ItemStoneListDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer, Sykra
 */
public class ItemSocketService {

	public static ManaStone addManaStone(Item item, int manaStoneItemId, boolean useFusionSlots) {
		if (item == null)
			return null;
		int maxSlots = item.getSockets(useFusionSlots);
		Set<ManaStone> manaStones = useFusionSlots ? item.getFusionStones() : item.getItemStones();
		if (manaStones.size() > maxSlots)
			return null;
		ItemGroup manaStoneCategory = DataManager.ITEM_DATA.getItemTemplate(manaStoneItemId).getItemGroup();
		int specialSlotCount = useFusionSlots ? item.getFusionedItemTemplate().getSpecialSlots() : item.getItemTemplate().getSpecialSlots();
		return insertManaStoneIntoNextPossibleSlot(item, manaStoneItemId, maxSlots, manaStones, manaStoneCategory, specialSlotCount);
	}

	public static ManaStone addManaStone(Item item, int manaStoneItemId, int slotId, boolean useFusionSlots) {
		if (item == null)
			return null;
		Set<ManaStone> manaStones = useFusionSlots ? item.getFusionStones() : item.getItemStones();
		if (manaStones.size() >= Item.MAX_BASIC_STONES)
			return null;
		return insertManastoneIntoSlot(item, manaStones, manaStoneItemId, slotId);
	}

	private static ManaStone insertManaStoneIntoNextPossibleSlot(Item item, int manaStoneItemId, int maxSlots, Set<ManaStone> manaStones,
		ItemGroup manastoneCategory, int specialSlotCount) {
		if (manastoneCategory == ItemGroup.SPECIAL_MANASTONE && specialSlotCount == 0)
			return null;

		int specialSlotsOccupied = 0;
		int normalSlotsOccupied = 0;
		Set<Integer> allSlots = new HashSet<>();
		for (ManaStone ms : manaStones) {
			ItemGroup category = DataManager.ITEM_DATA.getItemTemplate(ms.getItemId()).getItemGroup();
			if (category == ItemGroup.SPECIAL_MANASTONE)
				specialSlotsOccupied++;
			else
				normalSlotsOccupied++;
			allSlots.add(ms.getSlot());
		}

		if ((manastoneCategory == ItemGroup.SPECIAL_MANASTONE && specialSlotsOccupied >= specialSlotCount)
			|| (manastoneCategory == ItemGroup.MANASTONE && normalSlotsOccupied >= (maxSlots - specialSlotCount)))
			return null;

		int start = manastoneCategory == ItemGroup.SPECIAL_MANASTONE ? 0 : specialSlotCount;
		int end = manastoneCategory == ItemGroup.SPECIAL_MANASTONE ? specialSlotCount : maxSlots;
		int nextSlot = start;
		boolean slotFound = false;
		for (; nextSlot < end; nextSlot++) {
			if (!allSlots.contains(nextSlot)) {
				slotFound = true;
				break;
			}
		}
		if (!slotFound)
			return null;
		return insertManastoneIntoSlot(item, manaStones, manaStoneItemId, nextSlot);
	}

	private static ManaStone insertManastoneIntoSlot(Item item, Set<ManaStone> manaStones, int manastoneId, int slotId) {
		item.removeRemainingTuningCountIfPossible();
		ManaStone stone = new ManaStone(item.getObjectId(), manastoneId, slotId, PersistentState.NEW);
		manaStones.add(stone);
		return stone;
	}

	public static void copyFusionStones(Item source, Item target) {
		if (source.hasManaStones()) {
			for (ManaStone manaStone : source.getItemStones())
				target.getFusionStones().add(new ManaStone(target.getObjectId(), manaStone.getItemId(), manaStone.getSlot(), PersistentState.NEW));
			target.removeRemainingTuningCountIfPossible();
		}
	}

	public static void removeManastone(Player player, int itemObjId, int slotNum, boolean isFusionSocket) {
		Storage inventory = player.getInventory();
		Item item = inventory.getItemByObjId(itemObjId);
		if (item == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REMOVE_ITEM_OPTION_NO_TARGET_ITEM());
			return;
		}

		boolean hasManaStones = isFusionSocket ? item.hasFusionStones() : item.hasManaStones();
		if (!hasManaStones) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REMOVE_ITEM_OPTION_NO_OPTION_TO_REMOVE(item.getL10n()));
			return;
		}

		Set<ManaStone> itemStones = isFusionSocket ? item.getFusionStones() : item.getItemStones();
		ManaStone manaStoneToRemove = itemStones.stream().filter(ms -> ms.getSlot() == slotNum).findFirst().orElse(null);
		if (manaStoneToRemove == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REMOVE_ITEM_OPTION_INVALID_OPTION_SLOT_NUMBER(item.getL10n()));
			return;
		}

		long price = PricesService.getPriceForService(650, player.getRace());
		if (player.getInventory().tryDecreaseKinah(price)) {
			manaStoneToRemove.setPersistentState(PersistentState.DELETED);
			if (isFusionSocket) {
				ItemStoneListDAO.storeFusionStone(Collections.singleton(manaStoneToRemove));
			} else {
				ItemStoneListDAO.storeManaStones(Collections.singleton(manaStoneToRemove));
			}
			itemStones.remove(manaStoneToRemove);

			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REMOVE_ITEM_OPTION_SUCCEED(item.getL10n()));
			ItemPacketService.updateItemAfterInfoChange(player, item);
		} else {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REMOVE_ITEM_OPTION_NOT_ENOUGH_GOLD(item.getL10n()));
		}
	}

	public static void removeAllManastone(Player player, Item item) {
		if (item == null || !item.hasManaStones())
			return;

		Set<ManaStone> itemStones = item.getItemStones();
		for (ManaStone ms : itemStones)
			ms.setPersistentState(PersistentState.DELETED);
		ItemStoneListDAO.storeManaStones(itemStones);
		itemStones.clear();

		ItemPacketService.updateItemAfterInfoChange(player, item);
	}

	public static void socketGodstone(Player player, Item weapon, int stoneId) {
		if (weapon == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_PROC_NO_TARGET_ITEM());
			return;
		}

		if (!weapon.canSocketGodstone()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_PROC_NOT_PROC_GIVABLE_ITEM(weapon.getL10n()));
			AuditLogger.log(player, "tried to insert godstone in not compatible item " + weapon.getItemId());
			return;
		}

		final StartMovingListener move = new StartMovingListener() {

			@Override
			public void moved() {
				super.moved();
				player.getObserveController().removeObserver(this);
				player.getController().cancelUseItem();
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GIVE_PROC_CANCEL(weapon.getL10n()));

			}
		};

		player.getObserveController().attach(move);

		Item godstone = player.getInventory().getItemByObjId(stoneId);
		if (godstone == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_PROC_NO_PROC_GIVE_ITEM());
			return;
		}

		ItemTemplate itemTemplate = godstone.getItemTemplate();
		if (itemTemplate.getGodstoneInfo() == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_PROC_NO_PROC_GIVE_ITEM());
			return;
		}

		PacketSendUtility.broadcastPacketAndReceive(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), stoneId, itemTemplate.getTemplateId(), 2000, 0, 0));

		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(() -> {
			player.getObserveController().removeObserver(move);

			PacketSendUtility.broadcastPacketAndReceive(player,
				new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), stoneId, itemTemplate.getTemplateId(), 0, 1, 0));

			if (!player.getInventory().decreaseByObjectId(stoneId, 1))
				return;

			weapon.addGodStone(itemTemplate.getTemplateId());
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_PROC_ENCHANTED_TARGET_ITEM(weapon.getL10n()));

			ItemPacketService.updateItemAfterInfoChange(player, weapon);
		}, 2000));
	}
}
