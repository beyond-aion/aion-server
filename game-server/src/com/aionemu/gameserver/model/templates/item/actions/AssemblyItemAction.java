package com.aionemu.gameserver.model.templates.item.actions;

import static com.aionemu.gameserver.model.items.ItemUseAnimation.*;

import java.util.Map;
import java.util.stream.Collectors;

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
		int castingDelay = parentItem.getItemTemplate().getCastingDelay();
		if (castingDelay <= 0) {
			finishUse(player, parentItem);
			return;
		}
		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), castingDelay, USE_START), true);
		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ASSEMBLY_ITEM_CANCELED());
				PacketSendUtility.broadcastPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem .getItemTemplate().getTemplateId(), 0, USE_FAIL),
					true);
				player.getObserveController().removeObserver(this);
			}

		};

		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(() -> {
			player.getObserveController().removeObserver(observer);
			finishUse(player, parentItem);
		}, castingDelay));
	}

	private void finishUse(Player player, Item parentItem) {
		AssemblyItem assemblyItem = getAssemblyItem();
		Map<Integer, Long> requiredCounts = assemblyItem.getParts().stream().collect(Collectors.groupingBy(id -> id, Collectors.counting()));
		for (Map.Entry<Integer, Long> requiredCount : requiredCounts.entrySet()) {
			if (player.getInventory().getItemCountByItemId(requiredCount.getKey()) < requiredCount.getValue()) {
				PacketSendUtility.broadcastPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem .getItemTemplate().getTemplateId(), 0, USE_FAIL),
					true);
				return;
			}
		}
		player.startCooldown(parentItem);
		for (Integer itemId : assemblyItem.getParts()) {
			player.getInventory().decreaseByItemId(itemId, 1);
		}
		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem .getItemTemplate().getTemplateId(), 0, USE_SUCCESS),
			true);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_USE_ITEM(parentItem.getL10n()));
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ASSEMBLY_ITEM_SUCCEEDED());
		ItemService.addItem(player, assemblyItem.getId(), 1);
	}

	public AssemblyItem getAssemblyItem() {
		return DataManager.ASSEMBLY_ITEM_DATA.getAssemblyItem(item);
	}
}
