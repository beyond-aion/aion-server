package com.aionemu.gameserver.model.templates.item.actions;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PortalCooldown;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Tiger
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InstanceTimeClear")
public class InstanceTimeClear extends AbstractItemAction {

	@XmlAttribute(name = "sync_ids")
	protected List<Integer> syncIds;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem) {
		// TODO: there's a dialog of selection of the instance you want to reset
		// Meanwhile, resest only those with CD
		boolean hasAnyCd = false;
		for (Integer syncId : syncIds) {
			int mapid = DataManager.INSTANCE_COOLTIME_DATA.getWorldId(syncId);
			if (player.getPortalCooldownList().getPortalCooldownTime(mapid) == 0) {
				// Notify that not able to reset, already don't have CD
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_COOL_TIME_INIT());
				if (syncIds.size() == 1) {
					return false;
				}
			} else {
				hasAnyCd = true;
			}
		}
		return hasAnyCd;
	}

	@Override
	public void act(final Player player, final Item parentItem, Item targetItem) {
		PacketSendUtility.broadcastPacketAndReceive(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 1000, 0, 0));

		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				// TODO: abort is invalid. Should we abort all or only the last syncid?
				player.getController().cancelTask(TaskId.ITEM_USE);
				player.removeItemCoolDown(parentItem.getItemTemplate().getUseLimits().getDelayId());
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED(new DescriptionId(parentItem.getItemTemplate().getNameId())));
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
				if (parentItem.getActivationCount() > 1) {
					parentItem.setActivationCount(parentItem.getActivationCount() - 1);
				} else {
					player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1);
				}

				for (Integer syncId : syncIds) {
					int mapid = DataManager.INSTANCE_COOLTIME_DATA.getWorldId(syncId);
					PortalCooldown portalCD = player.getPortalCooldownList().getPortalCooldown(mapid);
					if (portalCD == null || portalCD.getEnterCount() < 1)
						continue; // don't spam with not needed packets!

					portalCD.decreaseEnterCount();
					if (portalCD.getEnterCount() < 1)
						player.getPortalCooldownList().removePortalCooldown(mapid);

					if (player.isInTeam())
						player.getCurrentTeam().sendPackets(new SM_INSTANCE_INFO((byte) 2, player, mapid));
					else
						PacketSendUtility.sendPacket(player, new SM_INSTANCE_INFO((byte) 2, player, mapid));
				}
				PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, 1, 0));
			}

		}, 1000));
	}

}
