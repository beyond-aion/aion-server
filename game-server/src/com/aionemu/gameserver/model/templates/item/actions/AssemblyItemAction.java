package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.AssemblyItem;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssemblyItemAction")
public class AssemblyItemAction extends AbstractItemAction {

	@XmlAttribute
	private int item;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		AssemblyItem assemblyItem = getAssemblyItem();
		if (assemblyItem == null) {
			return false;
		}
		for (Integer itemId : assemblyItem.getParts()) {
			if (player.getInventory().getFirstItemByItemId(itemId) == null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void act(final Player player, final Item parentItem, Item targetItem, Object... params) {
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(),
			1000, 0, 0), true);
		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				player.removeItemCoolDown(parentItem.getItemTemplate().getUseLimits().getDelayId());
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED());
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem
					.getItemTemplate().getTemplateId(), 0, 2, 0), true);
				player.getObserveController().removeObserver(this);
			}

		};

		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {

				player.getObserveController().removeObserver(observer);
				player.getController().cancelTask(TaskId.ITEM_USE);
				AssemblyItem assemblyItem = getAssemblyItem();
				for (Integer itemId : assemblyItem.getParts()) {
					if (!player.getInventory().decreaseByItemId(itemId, 1)) {
						return;
					}
				}
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem
					.getItemTemplate().getTemplateId(), 0, 1, 0), true);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ASSEMBLY_ITEM_SUCCEEDED());
				ItemService.addItem(player, assemblyItem.getId(), 1);
			}

		}, 1000));

	}

	public AssemblyItem getAssemblyItem() {
		return DataManager.ASSEMBLY_ITEM_DATA.getAssemblyItem(item);
	}
}
