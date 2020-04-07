package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * Created with IntelliJ IDEA. User: pixfid Date: 7/14/13 Time: 5:18 PM
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompositionAction")
public class CompositionAction extends AbstractItemAction {

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		return false;
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem, Object... params) {

	}

	public boolean canAct(Player player, Item tools, Item first, Item second) {

		if (!tools.getItemTemplate().isCombinationItem())
			return false;

		if (!first.getItemTemplate().isEnchantmentStone())
			return false;

		if (!second.getItemTemplate().isEnchantmentStone())
			return false;

		if (first.getItemCount() < 1 || second.getItemCount() < 1)
			return false;

		if (first.getItemTemplate().getLevel() > 95 || second.getItemTemplate().getLevel() > 95)
			return false;

		return true;
	}

	public void act(final Player player, final Item tools, final Item first, final Item second) {

		PacketSendUtility.sendPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), tools.getObjectId(), tools.getItemTemplate()
			.getTemplateId(), 5000, 0, 0));
		player.getController().cancelTask(TaskId.ITEM_USE);

		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				PacketSendUtility.sendPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), tools.getObjectId(), tools.getItemTemplate()
					.getTemplateId(), 0, 2, 0));
				player.getObserveController().removeObserver(this);
			}
		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				player.getObserveController().removeObserver(observer);
				boolean result = player.getInventory().decreaseByItemId(tools.getItemId(), 1);
				boolean result1 = player.getInventory().decreaseByItemId(first.getItemId(), 1);
				boolean result2 = player.getInventory().decreaseByItemId(second.getItemId(), 1);
				if (result && result1 && result2) {
					ItemService.addItem(player, getItemId(calcLevel(first.getItemTemplate().getLevel(), second.getItemTemplate().getLevel())), 1);
				}
				PacketSendUtility.sendPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), tools.getObjectId(), tools.getItemTemplate()
					.getTemplateId(), 0, 1, 0));
			}
		}, 5000));

	}

	private int calcLevel(int first, int second) {
		int value = ((first + second) / 2);
		if (value < 11) {
			value = Rnd.get(1, 20);
		} else {
			int random = Rnd.get(1, 10);
			int bit = Rnd.get(0, 1);
			value = (bit == 0 ? value - random : value + random);
		}
		return value;
	}

	public int getItemId(int value) {
		return 166000000 + value;
	}
}
