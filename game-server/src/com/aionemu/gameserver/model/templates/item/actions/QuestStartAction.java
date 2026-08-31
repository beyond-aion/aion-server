package com.aionemu.gameserver.model.templates.item.actions;

import static com.aionemu.gameserver.model.items.ItemUseAnimation.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Nemiroff Date: 17.12.2009
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestStartAction")
public class QuestStartAction extends AbstractItemAction {

	@XmlAttribute
	protected int questid;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		// Retail always plays the cast; eligibility is only checked afterwards, in finishUse()
		return true;
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
		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED());
				PacketSendUtility.broadcastPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 0, USE_FAIL),
					true);
			}
		};

		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(() -> {
			player.getObserveController().removeObserver(observer);
			finishUse(player, parentItem);
		}, castingDelay));

	}

	private void finishUse(Player player, Item item) {
		player.startCooldown(item);
		PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), item.getObjectId(), item.getItemId()));

		// retail stays silent when the quest is already active or cannot be repeated (anymore), but warns about
		// race/level/etc. restrictions before sending the use message (confirmed on retail 5.8)
		QuestState qs = player.getQuestStateList().getQuestState(questid);
		boolean canStart = (qs == null || qs.isStartable()) && QuestService.checkStartConditions(player, questid, true, 0, true, true, false);

		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_USE_ITEM(item.getL10n()));

		if (!canStart)
			return; // quest not startable, or requirements not met (checkStartConditions already sent the message)

		// CM_USE_ITEM skips onItemUseEvent for QuestStartAction items so it doesn't fire before the cast; call it
		// here instead, falling back to the generic dialog routing if the item isn't a registered quest item
		QuestEnv env = new QuestEnv(null, player, questid, DialogAction.ASK_QUEST_ACCEPT);
		HandlerResult result = QuestEngine.getInstance().onItemUseEvent(env, item);
		if (result != HandlerResult.SUCCESS)
			QuestEngine.getInstance().onDialog(new QuestEnv(null, player, questid, DialogAction.ASK_QUEST_ACCEPT));
	}
}
