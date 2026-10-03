package com.aionemu.gameserver.model.templates.item.actions;

import static com.aionemu.gameserver.model.items.ItemUseAnimation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.templates.item.*;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FIRST_SHOW_DECOMPOSABLE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author oslo(a00441234)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DecomposeAction")
public class DecomposeAction extends AbstractItemAction {

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		if (player.isDead() || !player.isSpawned())
			return false;
		DecomposableItemInfo info = DataManager.DECOMPOSABLE_ITEMS_DATA.getInfoByItemId(parentItem.getItemId());
		if (info == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_IT_CAN_NOT_BE_DECOMPOSED(parentItem.getL10n()));
			return false;
		}
		if (player.getInventory().isFull() || player.getInventory().isFullSpecialCube() && containsSpecialCubeItems(info, player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_WAREHOUSE_FULL_INVENTORY());
			return false;
		}
		return true;
	}

	@Override
	public void act(final Player player, final Item parentItem, final Item targetItem, Object... params) {
		DecomposableItemInfo info = DataManager.DECOMPOSABLE_ITEMS_DATA.getInfoByItemId(parentItem.getItemId());
		if (info.isSelectable()) {
			DecomposableSet set = info.getSelectableSet(player);
			if (set == null)
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANT_USE_ITEM(parentItem.getL10n()));
			else
				PacketSendUtility.sendPacket(player, new SM_FIRST_SHOW_DECOMPOSABLE(parentItem.getObjectId(), set.getItems()));
			return;
		}
		int castingDelay = parentItem.getItemTemplate().getCastingDelay();
		if (castingDelay <= 0) {
			finishUse(player, parentItem, targetItem, info);
			return;
		}
		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), castingDelay, USE_START), true);

		ItemUseObserver observer = new ItemUseObserver(player) {

			@Override
			protected void onAbort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_UNCOMPRESS_COMPRESSED_ITEM_CANCELED(parentItem.getL10n()));
				PacketSendUtility.broadcastPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 0, USE_CANCEL),
					true);
			}

		};

		player.getObserveController().addObserver(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(() -> {
			player.getObserveController().removeObserver(observer);
			finishUse(player, parentItem, targetItem, info);
		}, castingDelay));
	}

	private boolean postValidate(Player player, Item parentItem, Item targetItem) {
		if (!canAct(player, parentItem, targetItem)) {
			return false;
		}
		if (!player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_NO_TARGET_ITEM());
			return false;
		}
		return true;
	}

	private void finishUse(Player player, Item parentItem, Item targetItem, DecomposableItemInfo info) {
		Map<Integer, Long> rewards = mergeRewards(info.decompose(player));
		boolean obtainedRewards = !rewards.isEmpty() && !ownsLoreReward(player, rewards);
		boolean validAction = obtainedRewards && postValidate(player, parentItem, targetItem);
		if (validAction) {
			player.startCooldown(parentItem);
			for (Map.Entry<Integer, Long> reward : rewards.entrySet())
				ItemService.addItem(player, reward.getKey(), reward.getValue(), true,
					new ItemUpdatePredicate(ItemAddType.DECOMPOSABLE, ItemUpdateType.INC_ITEM_COLLECT));
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_UNCOMPRESS_COMPRESSED_ITEM_SUCCEEDED(parentItem.getL10n()));
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_USE_ITEM(parentItem.getL10n()));
		} else if (!obtainedRewards) { // nothing was rolled, or the reward was voided by the lore check that already reported itself
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANT_USE_ITEM(parentItem.getL10n()));
		}
		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, validAction ? USE_SUCCESS : USE_FAIL),
			true);
	}

	/**
	 * Branches that hit the same item stack their counts instead of granting it twice.
	 */
	private Map<Integer, Long> mergeRewards(List<DecomposedItem> rewards) {
		Map<Integer, Long> merged = new LinkedHashMap<>();
		for (DecomposedItem reward : rewards)
			merged.merge(reward.getItemId(), (long) reward.getCount(), Long::sum);
		return merged;
	}

	/**
	 * A limited possession item the player already owns voids the whole decompose, the parent item is not consumed.
	 */
	private boolean ownsLoreReward(Player player, Map<Integer, Long> rewards) {
		for (int itemId : rewards.keySet()) {
			ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(itemId);
			if (template.hasLimitOne() && (player.getInventory().getFirstItemByItemId(itemId) != null
				|| player.getStorage(StorageType.REGULAR_WAREHOUSE.getId()).getFirstItemByItemId(itemId) != null)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CAN_NOT_DISASSEMBLE_LORE_ITEM(template.getL10n()));
				return true;
			}
		}
		return false;
	}

	private boolean containsSpecialCubeItems(DecomposableItemInfo info, Player player) {
		for (DecomposableSet set : info.getSets()) {
			if (!set.isApplicableTo(player))
				continue;
			for (DecomposedReward reward : set.getRewards()) {
				for (DecomposedItem item : reward.getItems()) {
					if (DataManager.ITEM_DATA.getItemTemplate(item.getItemId()).getExtraInventoryId() > 0)
						return true;
				}
			}
		}
		return false;
	}
}
