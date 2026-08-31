package com.aionemu.gameserver.model.templates.item.actions;

import static com.aionemu.gameserver.model.items.ItemUseAnimation.*;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PortalCooldown;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Tiger, Neon
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InstanceTimeClear")
public class InstanceTimeClear extends AbstractItemAction {

	@XmlAttribute(name = "sync_ids")
	private List<Integer> syncIds;
	@XmlAttribute(name = "recovery_instance_count")
	private int recoveryInstanceCount = 1;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		int syncId = (int) params[0];
		if (!syncIds.contains(syncId)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_COOL_TIME_INIT());
			return false;
		}
		return true;
	}

	@Override
	public void act(final Player player, final Item parentItem, Item targetItem, Object... params) {
		int castingDelay = parentItem.getItemTemplate().getCastingDelay();
		int syncId = (int) params[0];
		if (castingDelay <= 0) {
			finishUse(player, parentItem, syncId);
			return;
		}
		PacketSendUtility.broadcastPacketAndReceive(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), castingDelay, USE_START));

		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED());
				PacketSendUtility.broadcastPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 0, USE_FAIL),
					true);
				player.getObserveController().removeObserver(this);
			}

		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(() -> {
			player.getObserveController().removeObserver(observer);
			finishUse(player, parentItem, syncId);
		}, castingDelay));
	}

	private void finishUse(Player player, Item parentItem, int syncId) {
		int worldId = DataManager.INSTANCE_COOLTIME_DATA.getWorldId(syncId);

		if (parentItem.getActivationCount() > 1) {
			if (player.getInventory().getItemByObjId(parentItem.getObjectId()) == null)
				return; // item was traded or sold during the casting delay
			parentItem.setActivationCount(parentItem.getActivationCount() - 1);
		} else if (!player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1))
			return;

		player.startCooldown(parentItem);

		PortalCooldown portalCD = player.getPortalCooldownList().getOrCreatePortalCooldown(worldId);
		if (portalCD != null) {
			portalCD.decreaseEnterCount(recoveryInstanceCount);
			player.getPortalCooldownList().sendEntryInfo(worldId);
		}
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_USE_ITEM(parentItem.getL10n()));
		PacketSendUtility.broadcastPacketAndReceive(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, USE_SUCCESS));
	}
}
