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
	protected int cost;
	@XmlAttribute(name = "percent")
	protected boolean isPercent;
	@XmlAttribute(name = "item_id")
	protected int itemId;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		if (player.getInventory().isFull()) {
			return false;
		}

		PlayerCommonData cd = player.getCommonData();

		long expShown = cd.getExpShown();

		int required;
		if (isPercent) {
			required = (int) ((long) cd.getExpNeed() * cost / 100L);
		} else {
			required = cost;
		}

		if (required <= 0) {
			return false;
		}

		return expShown >= required;
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

				int toDecrease;
				if (isPercent) {
					toDecrease = (int) ((long) player.getCommonData().getExpNeed() * cost / 100L);
				} else {
					toDecrease = cost;
				}

				PlayerCommonData cd = player.getCommonData();
				long currentExp = cd.getExp();
				long levelStartExp = DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(cd.getLevel());

				long newExp = currentExp - toDecrease;
				if (newExp < levelStartExp) {
					newExp = levelStartExp;
				}

				if (newExp == currentExp) {
					player.getController().cancelTask(TaskId.ITEM_USE);
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_INVALID_STANCE(parentItem.getL10n()));
					PacketSendUtility.sendPacket(player,
						new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(),
							parentItem.getItemTemplate().getTemplateId(), 0, 2, 0));
					return;
				}

				cd.setExp(newExp);

				ItemService.addItem(player, itemId, 1);
				player.getInventory().decreaseByItemId(parentItem.getItemId(), 1);

				PacketSendUtility.sendPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 0, 1, 0));
			}
		}, 5000));

	}
}
