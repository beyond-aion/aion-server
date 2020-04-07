package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ReturnLocList;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ginho1
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MultiReturnAction")
public class MultiReturnAction extends AbstractItemAction {

	@XmlAttribute(name = "id")
	protected int id;

	@XmlTransient
	private static final short USAGE_DELAY = 5000;

	@Override
	public boolean canAct(Player player, Item item, Item targetItem, Object... params) {
		return true;
	}

	@Override
	public void act(final Player player, final Item item, final Item targetItem, Object... params) {
		int indexReturn = (int) params[0];
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), item.getObjectId(), item.getItemId(), USAGE_DELAY, 0, 0), true);

		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				player.removeItemCoolDown(item.getItemTemplate().getUseLimits().getDelayId());
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED());
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), item.getObjectId(), item.getItemId(), 0, 2, 0), true);
				player.getObserveController().removeObserver(this);
			}
		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				ReturnLocList loc = DataManager.MULTIRETURN_DATA.getReturnLocListById(id).get(indexReturn);
				if (loc != null && loc.getAlias() != null && loc.getWorldid() > 0) {
					if (!player.getInventory().decreaseByObjectId(item.getObjectId(), 1)) {
						observer.abort();
						return;
					}
					player.getObserveController().removeObserver(observer);
					TeleportService.useTeleportScroll(player, loc.getAlias().toUpperCase(), loc.getWorldid());
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_USE_ITEM(item.getL10n()));
				}
			}

		}, USAGE_DELAY));
	}
}
