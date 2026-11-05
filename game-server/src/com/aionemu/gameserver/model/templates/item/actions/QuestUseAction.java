package com.aionemu.gameserver.model.templates.item.actions;

import static com.aionemu.gameserver.model.items.ItemUseAnimation.*;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * Items which advance a quest step when used, the counterpart of {@link QuestStartAction} for quests that are already running. The client data
 * marks them with quest_label 2 and gives them a casting delay, but no effect of their own, since what happens is up to the quest.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestUseAction")
public class QuestUseAction extends AbstractItemAction {

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		return true; // like QuestStartAction, the cast plays first and the quest decides afterwards
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem, Object... params) {
		int castingDelay = parentItem.getItemTemplate().getCastingDelay();
		if (castingDelay <= 0) {
			finishUse(player, parentItem);
			return;
		}
		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), castingDelay, USE_START), true);
		ItemUseObserver observer = new ItemUseObserver(player) {

			@Override
			protected void onAbort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED());
				PacketSendUtility.broadcastPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 0, USE_CANCEL),
					true);
			}
		};

		player.getObserveController().addObserver(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(() -> {
			player.getObserveController().removeObserver(observer);
			finishUse(player, parentItem);
		}, castingDelay));
	}

	private void finishUse(Player player, Item item) {
		player.startCooldown(item);
		PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), item.getObjectId(), item.getItemId()));
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_USE_ITEM(item.getL10n()));
		// CM_USE_ITEM skips onItemUseEvent for these items so it doesn't fire before the cast, the quest is notified here instead
		QuestEngine.getInstance().onItemUseEvent(new QuestEnv(null, player, 0), item);
	}
}
