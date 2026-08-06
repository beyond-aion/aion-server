package com.aionemu.gameserver.model.templates.item.actions;

import java.util.Collection;
import java.util.Collections;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ChargeInfo;
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
		return !getConditioningItems(player, parentItem, targetItem).isEmpty();
	}

	/**
	 * @return The items to condition (just {@code targetItem} if one was selected), sending the appropriate "not chargeable" message if there are none.
	 */
	private Collection<Item> getConditioningItems(Player player, Item parentItem, Item targetItem) {
		int chargeWay = parentItem.getImprovement().getChargeWay();
		if (targetItem != null) {
			if (targetItem.getImprovement() == null || targetItem.getImprovement().getChargeWay() != chargeWay
				|| targetItem.calculateAvailableChargeLevel(player) == 0) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_CHARGE_FAIL_NOT_CHARGEABLE(targetItem.getL10n()));
				return Collections.emptyList();
			}
			int achievableLevel = ItemChargeService.calculateMaxChargeLevelBasedOnRank(player, targetItem, maxChargeLevel);
			int achievableChargePoints = achievableLevel == 1 ? ChargeInfo.LEVEL1 : ChargeInfo.LEVEL2;
			if (targetItem.getChargePoints() >= achievableChargePoints) {
				PacketSendUtility.sendPacket(player,
					SM_SYSTEM_MESSAGE.STR_MSG_ITEM_CHARGE_FAIL_ALREADY_CHARGED(targetItem.getL10n(), String.valueOf(achievableLevel)));
				return Collections.emptyList();
			}
			return Collections.singletonList(targetItem);
		}
		Collection<Item> conditioningItems = ItemChargeService.filterItemsToCondition(player, null, chargeWay);
		if (conditioningItems.isEmpty()) {
			if (chargeWay == 1)
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_CHARGE_ALL_FAIL_NO_CHARGEABLE_EQUIPMENT());
			else
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_CHARGE2_ALL_FAIL_NO_CHARGEABLE_EQUIPMENT());
		}
		return conditioningItems;
	}

	@Override
	public void act(final Player player, Item parentItem, final Item targetItem, Object... params) {
		int chargeWay = parentItem.getImprovement().getChargeWay();
		int castingDelay = parentItem.getItemTemplate().getCastingDelay();
		if (castingDelay <= 0) {
			finishUse(player, parentItem, targetItem);
			return;
		}
		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), castingDelay, 0, 0), true);
		ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelUseItem(false);
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
			finishUse(player, parentItem, targetItem);
		}, castingDelay));
	}

	private void finishUse(Player player, Item parentItem, Item targetItem) {
		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, 1, 0), true);
		Collection<Item> conditioningItems = getConditioningItems(player, parentItem, targetItem);
		if (conditioningItems.isEmpty())
			return;
		if (!player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1))
			return;
		if (targetItem != null) // avoid the "Successfully conditioned equipped item(s)" bulk summary for a single targeted item
			ItemChargeService.chargeItem(player, targetItem, maxChargeLevel, false, false);
		else
			ItemChargeService.chargeItems(player, conditioningItems, maxChargeLevel, false, false);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_USE_ITEM(parentItem.getL10n()));
	}

}
