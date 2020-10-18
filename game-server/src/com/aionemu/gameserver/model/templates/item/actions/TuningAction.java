package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.PendingTuneResult;
import com.aionemu.gameserver.model.templates.item.bonuses.StatBonusType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TUNE_RESULT;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TuningAction")
public class TuningAction extends AbstractItemAction {

	@XmlAttribute
	private UseTarget target;

	@XmlAttribute(name = "no_reduce")
	private boolean shouldNotReduceTuneCount;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		if (targetItem.isEquipped())
			return false;
		if (!targetItem.isIdentified()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_REIDENTIFY_DIDNT_IDENTIFY(targetItem.getL10n()));
			return false;
		}
		if (!targetItem.getItemTemplate().canTune()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_REIDENTIFY_CANNOT_REIDENTIFY(targetItem.getL10n()));
			return false;
		}
		if (target == UseTarget.WEAPON && !targetItem.getItemTemplate().isWeapon()
			|| target == UseTarget.ARMOR && !targetItem.getItemTemplate().isArmor()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_REIDENTIFY_WRONG_SELECT(parentItem.getL10n(), targetItem.getL10n()));
			return false;
		}
		if (targetItem.getItemTemplate().getLevel() > parentItem.getItemTemplate().getLevel()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_REIDENTIFY_WRONG_LEVEL(parentItem.getL10n(), targetItem.getL10n()));
			return false;
		}

		return shouldNotReduceTuneCount || targetItem.getTuneCount() < targetItem.getItemTemplate().getMaxTuneCount();
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem, Object... params) {
		int tuningScrollItemId = parentItem.getItemId();
		int tuningScrollObjectId = parentItem.getObjectId();
		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), tuningScrollItemId, 5000, 12, 0), true);
		ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				player.removeItemCoolDown(parentItem.getItemTemplate().getUseLimits().getDelayId());
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_REIDENTIFY_CANCELED(targetItem.getL10n()));
				PacketSendUtility.broadcastPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), tuningScrollObjectId, tuningScrollItemId, 0, 14, 0), true);
				player.getObserveController().removeObserver(this);
			}

		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(() -> {
			player.getObserveController().removeObserver(observer);
			PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), tuningScrollObjectId, tuningScrollItemId, 0, 13, 0),
				true);
			if (!player.getInventory().decreaseByObjectId(tuningScrollObjectId, 1))
				return;

			int newOptionalSockets, newEnchantBonus, newStatBonusId;
			if (shouldNotReduceTuneCount) { // only tune attributes (bonus stats)
				newOptionalSockets = targetItem.getOptionalSockets();
				newEnchantBonus = targetItem.getEnchantBonus();
			} else {
				targetItem.setTuneCount(targetItem.getTuneCount() + 1);
				player.getInventory().setPersistentState(Persistable.PersistentState.UPDATE_REQUIRED);
				newOptionalSockets = Rnd.get(0, targetItem.getItemTemplate().getOptionSlotBonus());
				newEnchantBonus = Rnd.get(0, targetItem.getItemTemplate().getMaxEnchantBonus());
			}
			newStatBonusId = getRandomStatBonusIdFor(targetItem);
			PendingTuneResult result = new PendingTuneResult(newOptionalSockets, newEnchantBonus, newStatBonusId, shouldNotReduceTuneCount);
			targetItem.setPendingTuneResult(result);
			PacketSendUtility.sendPacket(player, new SM_TUNE_RESULT(targetItem, tuningScrollItemId, result));
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_REIDENTIFY_SUCCEED(targetItem.getL10n()));
		}, 5000));
	}

	public static int getRandomStatBonusIdFor(Item item) {
		return DataManager.ITEM_RANDOM_BONUSES.selectRandomBonusNumber(StatBonusType.INVENTORY, item.getItemTemplate().getStatBonusSetId());
	}
}
