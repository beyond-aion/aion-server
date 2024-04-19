package com.aionemu.gameserver.services.item;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.PendingTuneResult;
import com.aionemu.gameserver.model.templates.item.actions.TuningAction;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author Estrayl
 */
public class ItemActionService {

	public static void identifyItem(Player player, Item item) {
		int itemId = item.getItemId();
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), item.getObjectId(), itemId, 5000, 9, 0), true);
		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_IDENTIFY_CANCELED(item.getL10n()));
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), item.getObjectId(), itemId, 0, 11, 0), true);
				player.getObserveController().removeObserver(this);
			}

		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				player.getObserveController().removeObserver(observer);
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), item.getObjectId(), itemId, 0, 10, 0), true);
				item.setOptionalSockets(Rnd.get(0, item.getItemTemplate().getOptionSlotBonus()));
				item.setBonusStats(TuningAction.getRandomStatBonusIdFor(item), true);
				item.setEnchantBonus(Rnd.get(0, item.getItemTemplate().getMaxEnchantBonus()));
				item.setTuneCount(item.getTuneCount() + 1); // not tuned have count = -1
				player.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);
				PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, item));
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_IDENTIFY_SUCCEED(item.getL10n()));
			}

		}, 5000));
	}

	public static void applyTuneResult(Player player, Item item) {
		PendingTuneResult tuneResult = item.getPendingTuneResult();
		if (tuneResult == null) {
			AuditLogger.log(player, "attempted to apply a tune result without tuning the item beforehand.");
			return;
		}
		item.setOptionalSockets(tuneResult.getOptionalSockets());
		item.setEnchantBonus(tuneResult.getEnchantBonus());
		item.setBonusStats(tuneResult.getStatBonusId(), true);
		item.setPendingTuneResult(null);
		item.setPersistentState(PersistentState.UPDATE_REQUIRED);
		player.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);
	}
}
