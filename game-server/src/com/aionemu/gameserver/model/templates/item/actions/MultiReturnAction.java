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

	@Override
	public boolean canAct(Player player, Item item, Item targetItem, Object... params) {
		return getReturnLoc((int) params[0]) != null;
	}

	@Override
	public void act(final Player player, final Item item, final Item targetItem, Object... params) {
		int castingDelay = item.getItemTemplate().getCastingDelay();
		ReturnLocList loc = getReturnLoc((int) params[0]);
		if (castingDelay <= 0) {
			finishUse(player, item, loc);
			return;
		}
		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), item.getObjectId(), item.getItemId(), castingDelay, USE_START), true);

		ItemUseObserver observer = new ItemUseObserver(player) {

			@Override
			protected void onAbort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED());
				PacketSendUtility.broadcastPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), item.getObjectId(), item.getItemId(), 0, USE_CANCEL), true);
			}
		};
		player.getObserveController().addObserver(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(() -> {
			player.getObserveController().removeObserver(observer);
			finishUse(player, item, loc);
		}, castingDelay));
	}

	private void finishUse(Player player, Item item, ReturnLocList loc) {
		if (!player.getInventory().decreaseByObjectId(item.getObjectId(), 1))
			return;
		player.startCooldown(item);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_USE_ITEM(item.getL10n()));
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), item.getObjectId(), item.getItemId(), 0, USE_SUCCESS),
			true);
		TeleportService.useTeleportScroll(player, loc.getAlias().toUpperCase(), loc.getWorldid());
	}

	private ReturnLocList getReturnLoc(int index) {
		List<ReturnLocList> locs = DataManager.MULTIRETURN_DATA.getReturnLocListById(id);
		if (locs == null || index < 0 || index >= locs.size())
			return null;
		ReturnLocList loc = locs.get(index);
		return loc != null && loc.getAlias() != null && loc.getWorldid() > 0 ? loc : null;
	}
}
