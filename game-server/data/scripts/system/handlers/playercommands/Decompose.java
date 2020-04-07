package playercommands;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.actions.AbstractItemAction;
import com.aionemu.gameserver.model.templates.item.actions.DecomposeAction;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Neon
 */
public class Decompose extends PlayerCommand {

	public Decompose() {
		super("decompose", "Opens decomposable items.");

		setSyntaxInfo("<item> [count] - Decomposes the specified item (default: all, optional: number of items to decompose).");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0) {
			sendInfo(player);
			return;
		}

		int itemId = ChatUtil.getItemId(params[0]);
		ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(itemId);

		if (itemId == 0 || template == null) {
			sendInfo(player, "Invalid item.");
			return;
		}

		long count = Long.MAX_VALUE;
		if (params.length > 1) {
			try {
				 count = Long.parseLong(params[1]);
			} catch (NumberFormatException e) {
				sendInfo(player, "Invalid count.");
				return;
			}
		}

		if (player.getInventory().getItemCountByItemId(itemId) == 0) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_NO_TARGET_ITEM());
			return;
		}

		if (template.getActions() == null || template.getActions().getItemActions().isEmpty()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_IT_CAN_NOT_BE_DECOMPOSED(template.getL10n()));
			return;
		}

		if (player.getInventory().isFull()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_INVENTORY_IS_FULL());
			return;
		}

		startTask(player, itemId, count);
	}

	private void startTask(Player player, int itemId, long count) {
		player.getController().addTask(TaskId.SKILL_USE, ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			long remainingCount = Math.min(player.getInventory().getItemCountByItemId(itemId), count);
			long totalCount = 0;
			DecomposeAction decomposeAction = null;
			ItemUseObserver observer;

			{
				if (DataManager.DECOMPOSABLE_ITEMS_DATA.getSelectableItems(itemId) == null) { // exclude selectable decomposables
					for (AbstractItemAction action : DataManager.ITEM_DATA.getItemTemplate(itemId).getActions().getItemActions()) {
						if (action instanceof DecomposeAction) {
							decomposeAction = (DecomposeAction) action;
							break;
						}
					}
				}

				// use observer to abort task on move, attack, die, item use, etc.
				observer = new ItemUseObserver() {

					@Override
					public void itemused(Item item) {
						abort();
					}

					@Override
					public void abort() {
						cancelTask(player, observer, "Decomposing aborted: Processed " + Math.max(0, totalCount - 1) + "x " + ChatUtil.item(itemId) + ".");
					}
				};

				player.getObserveController().attach(observer);
			}

			@Override
			public void run() {
				if (decomposeAction == null) {
					cancelTask(player, observer, "This item cannot be processed.");
					return;
				}

				Item item = player.getInventory().getFirstItemByItemId(itemId);
				if (item == null || remainingCount <= 0) {
					cancelTask(player, observer, "Decomposing finished: Processed " + totalCount + "x " + ChatUtil.item(itemId) + ".");
					return;
				}

				if (!decomposeAction.canAct(player, item, item) || player.getInventory().getItemCountByItemId(itemId) < remainingCount) {
					cancelTask(player, observer, "Decomposing aborted: Processed " + totalCount + "x " + ChatUtil.item(itemId) + ".");
					return;
				}

				decomposeAction.act(player, item, item);
				remainingCount--;
				totalCount++;
			}
		}, 10, DecomposeAction.USAGE_DELAY + 100));
	}

	private void cancelTask(Player player, ItemUseObserver observer, String message) {
		player.getController().cancelTask(TaskId.SKILL_USE);
		player.getObserveController().removeObserver(observer);
		sendInfo(player, message);
	}
}
