package com.aionemu.gameserver.model.templates.item.actions;

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

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		int syncId = (int) params[0];
		if (!syncIds.contains(syncId))
			return false;
		int worldId = DataManager.INSTANCE_COOLTIME_DATA.getWorldId(syncId);
		PortalCooldown portalCooldown = player.getPortalCooldownList().getPortalCooldown(worldId);
		if (portalCooldown == null || (portalCooldown.getReuseTime() < System.currentTimeMillis() && portalCooldown.getEnterCount() == 0)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_COOL_TIME_INIT());
			return false;
		}
		return true;
	}

	@Override
	public void act(final Player player, final Item parentItem, Item targetItem, Object... params) {
		int syncId = (int) params[0];
		PacketSendUtility.broadcastPacketAndReceive(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 1000, 0, 0));

		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				// TODO: abort is invalid. Should we abort all or only the last syncid?
				player.getController().cancelTask(TaskId.ITEM_USE);
				player.removeItemCoolDown(parentItem.getItemTemplate().getUseLimits().getDelayId());
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED());
				PacketSendUtility.broadcastPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 0, 2, 0), true);
				player.getObserveController().removeObserver(this);
			}

		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				player.getObserveController().removeObserver(observer);
				if (parentItem.getActivationCount() > 1) {
					parentItem.setActivationCount(parentItem.getActivationCount() - 1);
				} else {
					player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1);
				}

				int worldId = DataManager.INSTANCE_COOLTIME_DATA.getWorldId(syncId);
				PortalCooldown portalCD = player.getPortalCooldownList().getPortalCooldown(worldId);
				if (portalCD == null || portalCD.getEnterCount() < 1)
					return; // don't spam with not needed packets!

				portalCD.decreaseEnterCount();
				if (portalCD.getEnterCount() < 1)
					player.getPortalCooldownList().removePortalCooldown(worldId);

				player.getPortalCooldownList().sendEntryInfo(worldId);
				PacketSendUtility.broadcastPacketAndReceive(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, 1, 0));
			}

		}, 1000));
	}

}
