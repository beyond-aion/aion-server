package com.aionemu.gameserver.model.templates.item.actions;

import static com.aionemu.gameserver.model.items.ItemUseAnimation.*;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * Grants XP: a fixed amount, or a percentage of the XP the player's current level needs. Bonuses such as repose energy or XP rates do not
 * apply.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExpAddAction")
public class ExpAddAction extends AbstractItemAction {

	@XmlAttribute
	private long points;
	@XmlAttribute
	private int percent;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		return true;
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem, Object... params) {
		int castingDelay = player.getItemUseDelay(parentItem.getItemTemplate().getCastingDelay());
		if (castingDelay <= 0) {
			finishUse(player, parentItem);
			return;
		}
		PacketSendUtility.sendPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), castingDelay, USE_START));
		ItemUseObserver observer = new ItemUseObserver(player) {

			@Override
			protected void onAbort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				PacketSendUtility.sendPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0,
					USE_CANCEL));
			}
		};
		player.getObserveController().addObserver(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(() -> {
			player.getObserveController().removeObserver(observer);
			finishUse(player, parentItem);
		}, castingDelay));
	}

	private void finishUse(Player player, Item parentItem) {
		if (!player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1)) {
			PacketSendUtility.sendPacket(player,
				new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, USE_FAIL));
			return;
		}
		player.startCooldown(parentItem);
		PlayerCommonData cd = player.getCommonData();
		long exp = percent > 0 ? cd.getExpNeed() * percent / 100 : points;
		if (exp > 0 && !cd.getNoExp()) {
			cd.setExp(cd.getExp() + exp);
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ADDEXP_POINT_USE(parentItem.getL10n(), exp));
		}
		PacketSendUtility.sendPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, exp > 0 ? USE_SUCCESS : USE_FAIL));
	}
}
