package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATUPDATE_EXP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rolandas
 */
public class ExpExtractAction extends AbstractItemAction {

	@XmlAttribute
	protected int cost;
	@XmlAttribute(name = "percent")
	protected boolean isPercent;
	@XmlAttribute(name = "item_id")
	protected int itemId;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		if (player.getCommonData().getExp() == 0) {
			return false;
		}
		if (player.getInventory().isFull()) {
			return false;
		}
		return true;
	}

	@Override
	public void act(final Player player, final Item parentItem, Item targetItem, Object... params) {
		PacketSendUtility.sendPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 5000, 0, 0));
		player.getController().cancelTask(TaskId.ITEM_USE);
		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_CANCELED(parentItem.getL10n()));
				PacketSendUtility.sendPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 0, 2, 0));
				player.getObserveController().removeObserver(this);
			}
		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				player.getObserveController().removeObserver(observer);
				int toDecrease = 0;
				if (isPercent) {
					toDecrease = (int) (player.getCommonData().getExpNeed() / 100) * cost;
				} else {
					toDecrease = cost;
				}
				player.getCommonData().setExp(player.getCommonData().getExp() - toDecrease);
				ItemService.addItem(player, itemId, 1);
				player.getInventory().decreaseByItemId(parentItem.getItemId(), 1);
				PacketSendUtility.sendPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 0, 1, 0));
			}
		}, 5000));
		PacketSendUtility.sendPacket(player, new SM_STATUPDATE_EXP(player.getCommonData().getExpShown(), player.getCommonData().getExpRecoverable(),
			player.getCommonData().getExpNeed(), player.getCommonData().getCurrentReposeEnergy(), player.getCommonData().getMaxReposeEnergy()));

	}

}
