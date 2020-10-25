package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

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

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		if (targetItem == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_NO_TARGET_ITEM());
			return false;
		}
		if (!targetItem.getItemTemplate().isArmor() && !targetItem.getItemTemplate().isWeapon()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_IT_CAN_NOT_BE_DECOMPOSED(targetItem.getL10n()));
			return false;
		}
		if (targetItem.isEquipped()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_EQUIP_ITEM_CAN_NOT_BE_DECOMPOSED());
			return false;
		}

		return true;
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem, Object... params) {
		PacketSendUtility.sendPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate()
			.getTemplateId(), 5000, 0, 0));
		player.getController().cancelTask(TaskId.ITEM_USE);
		ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_CANCELED(targetItem.getL10n()));
				PacketSendUtility.sendPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate()
					.getTemplateId(), 0, 2, 0));
				player.getObserveController().removeObserver(this);
			}
		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(() -> {
			player.getObserveController().removeObserver(observer);
			boolean result = EnchantService.breakItem(player, targetItem, parentItem);
			PacketSendUtility.sendPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate()
				.getTemplateId(), 0, result ? 1 : 2, 0));
		}, 5000));
	}

}
