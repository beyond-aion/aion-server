package com.aionemu.gameserver.model.templates.item.actions;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemChargeService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChargeItemAction")
public class ChargeAction extends AbstractItemAction {

	@XmlAttribute(name = "capacity")
	private int maxChargeLevel;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		return !ItemChargeService.filterItemsToCondition(player, null, parentItem.getImprovement().getChargeWay()).isEmpty();
	}

	@Override
	public void act(final Player player, Item parentItem, Item targetItem, Object... params) {
		int chargeWay = parentItem.getImprovement().getChargeWay();
		Collection<Item> conditioningItems = ItemChargeService.filterItemsToCondition(player, null, chargeWay);

		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 3000, 0, 0), true);
		ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				if (chargeWay == 1)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_CHARGE_CANCELED());
				else
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_CHARGE2_CANCELED());
				PacketSendUtility.broadcastPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, 1, 0), true);
				player.getObserveController().removeObserver(this);
			}

		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(() -> {
			player.getObserveController().removeObserver(observer);
			PacketSendUtility.broadcastPacket(player,
				new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, 1, 0), true);
			if (!player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1))
				return;
			ItemChargeService.chargeItems(player, conditioningItems, maxChargeLevel, false, false);
		}, 3000));
	}

}
