package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TuningAction")
public class TuningAction extends AbstractItemAction {

	@XmlAttribute
	UseTarget target;

	@XmlAttribute(name = "no_reduce")
	boolean noReduce;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem) {
		if (!targetItem.getItemTemplate().canTune()) {
			return false;
		}
		if (target.equals(UseTarget.WEAPON) && !targetItem.getItemTemplate().isWeapon()) {
			return false;
		}
		if (target.equals(UseTarget.ARMOR) && !targetItem.getItemTemplate().isArmor()) {
			return false;
		}
		int randomCount = targetItem.getRandomCount();
		return (randomCount == -1 || randomCount < targetItem.getItemTemplate().getRandomBonusCount()) && !targetItem.isEquipped();
	}

	@Override
	public void act(final Player player, final Item parentItem, final Item targetItem) {
		final int parentItemId = parentItem.getItemId();
		final int parntObjectId = parentItem.getObjectId();
		final int parentNameId = parentItem.getNameId();
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItemId, 5000, 9, 0),
			true);
		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				player.removeItemCoolDown(parentItem.getItemTemplate().getUseLimits().getDelayId());
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED(new DescriptionId(parentNameId)));
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parntObjectId, parentItemId, 0, 11, 0), true);
				player.getObserveController().removeObserver(this);
			}

		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				player.getObserveController().removeObserver(observer);
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parntObjectId, parentItemId, 0, 10, 0), true);
				if (!player.getInventory().decreaseByObjectId(parntObjectId, 1)) {
					return;
				}
				int rndCount = targetItem.getRandomCount();
				if (rndCount > 0 && rndCount >= targetItem.getItemTemplate().getRandomBonusCount() || targetItem.isEquipped()) {
					return;
				}
				int newSockets = Rnd.get(0, targetItem.getItemTemplate().getOptionSlotBonus());
				if (noReduce && targetItem.getOptionalSocket() > newSockets)
					newSockets = targetItem.getOptionalSocket();
				targetItem.setOptionalSocket(newSockets);
				// TODO: how we should handle that ?
				targetItem.setRndBonus();
				int newEnchantBonus = Rnd.get(0, targetItem.getItemTemplate().getMaxEnchantBonus());
				if (noReduce && targetItem.getEnchantBonus() > newEnchantBonus)
					newEnchantBonus = targetItem.getEnchantBonus();
				targetItem.setEnchantBonus(newEnchantBonus);
				// not tuned have count = -1
				targetItem.setRandomCount(targetItem.getRandomCount() + 1);
				if (targetItem.getRandomCount() == 0 && targetItem.getItemTemplate().getRandomBonusCount() > 0)
					targetItem.setRandomCount(targetItem.getRandomCount() + 1);
				targetItem.setPersistentState(PersistentState.UPDATE_REQUIRED);
				player.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);
				PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, targetItem));
			}

		}, 5000));

	}

}
