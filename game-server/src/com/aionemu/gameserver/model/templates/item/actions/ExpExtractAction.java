package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rolandas, daddycaddy
 */
public class ExpExtractAction extends AbstractItemAction {

	@XmlAttribute
	private long cost;
	@XmlAttribute(name = "percent")
	private boolean isPercent;
	@XmlAttribute(name = "item_id")
	private int itemId;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		PlayerCommonData cd = player.getCommonData();
		long newExp = cd.getExp() - getRequiredExp(cd);
		return canExtractExp(player, newExp);
	}

	private boolean canExtractExp(Player player, long newExp) {
		if (player.getInventory().isFull()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DECOMPRESS_INVENTORY_IS_FULL());
			return false;
		}
		if (newExp < DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(player.getLevel())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EXP_EXTRACTION_USE_NOT_ENOUGH_EXP());
			return false;
		}
		return true;
	}

	@Override
	public void act(final Player player, final Item parentItem, Item targetItem, Object... params) {
		PacketSendUtility.sendPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 5000, 0, 0));

		player.getController().cancelTask(TaskId.ITEM_USE);

		final ItemUseObserver observer = new ItemUseObserver() {
			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_CANCELED(parentItem.getL10n()));
				PacketSendUtility.sendPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 0, 2, 0));
				player.getObserveController().removeObserver(this);
			}
		};

		player.getObserveController().attach(observer);

		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				player.getObserveController().removeObserver(observer);

				PlayerCommonData cd = player.getCommonData();
				long requiredExp = getRequiredExp(cd);
				long newExp = cd.getExp() - requiredExp;
				if (!canExtractExp(player, newExp) || !player.getInventory().decreaseByItemId(parentItem.getItemId(), 1)) {
					player.getController().cancelTask(TaskId.ITEM_USE);
					PacketSendUtility.sendPacket(player,
						new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(),
							parentItem.getItemTemplate().getTemplateId(), 0, 2, 0));
					return;
				}

				cd.setExp(newExp);
				ItemService.addItem(player, itemId, 1);
				String rewardItem = DataManager.ITEM_DATA.getItemTemplate(itemId).getL10n();
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EXP_EXTRACTION_USE(parentItem.getL10n(), requiredExp, rewardItem));
				PacketSendUtility.sendPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 0, 1, 0));
			}
		}, 5000));
	}

	private long getRequiredExp(PlayerCommonData cd) {
		if (isPercent) {
			return Math.max(1, cd.getExpNeed() * cost / 100L);
		}
		return cost;
	}
}
