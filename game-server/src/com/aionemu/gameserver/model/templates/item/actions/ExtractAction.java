package com.aionemu.gameserver.model.templates.item.actions;

import static com.aionemu.gameserver.model.items.ItemUseAnimation.*;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.EnchantService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExtractAction")
public class ExtractAction extends AbstractItemAction {

	private static final int MAX_EXTRACTABLE_LEVEL = 80;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		if (targetItem == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_NO_TARGET_ITEM());
			return false;
		}
		int itemId = targetItem.getItemId();
		if (!isExtractableItemId(itemId)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_IT_CAN_NOT_BE_DECOMPOSED(targetItem.getL10n()));
			return false;
		}
		if (targetItem.getItemTemplate().isCannotExtraction()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_IT_CAN_NOT_BE_DECOMPOSED(targetItem.getL10n()));
			return false;
		}
		if (targetItem.getItemTemplate().getLevel() > MAX_EXTRACTABLE_LEVEL) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_IT_CAN_NOT_BE_DECOMPOSED(targetItem.getL10n()));
			return false;
		}
		if (targetItem.isEquipped()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_EQUIP_ITEM_CAN_NOT_BE_DECOMPOSED());
			return false;
		}

		return true;
	}

	private static boolean isExtractableItemId(int itemId) {
		return (itemId >= 100000000 && itemId <= 102999999) || (itemId >= 110000000 && itemId <= 118999999);
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem, Object... params) {
		int useDelay = player.getItemUseDelay(5000);
		PacketSendUtility.sendPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), useDelay, USE_START));
		ItemUseObserver observer = new ItemUseObserver(player) {

			@Override
			protected void onAbort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_CANCELED(targetItem.getL10n()));
				PacketSendUtility.sendPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 0, USE_CANCEL));
			}
		};
		player.getObserveController().addObserver(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(() -> {
			player.getObserveController().removeObserver(observer);
			boolean result = canAct(player, parentItem, targetItem) && EnchantService.breakItem(player, targetItem, parentItem);
			if (result)
				// The only item with an extract action has no use delay, so this is effectively
				// a no-op, but kept for consistency with the other actions.
				player.startCooldown(parentItem);
			PacketSendUtility.sendPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(),
				parentItem.getItemTemplate().getTemplateId(), 0, result ? USE_SUCCESS : USE_FAIL));
		}, useDelay));
	}

}
