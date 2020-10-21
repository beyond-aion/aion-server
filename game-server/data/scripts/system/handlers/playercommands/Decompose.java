package playercommands;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.actions.DecomposeAction;
import com.aionemu.gameserver.model.templates.item.actions.ItemActions;
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

		Item item = player.getInventory().getFirstItemByItemId(ChatUtil.getItemId(params[0]));
		if (item == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_NO_TARGET_ITEM());
			return;
		}
		long count = params.length == 1 ? Long.MAX_VALUE : Long.parseLong(params[1]);

		ItemActions itemActions = item.getItemTemplate().getActions();
		DecomposeAction decomposeAction = itemActions == null ? null
			: itemActions.getItemActions().stream().filter(a -> a instanceof DecomposeAction).map(DecomposeAction.class::cast).findAny().orElse(null);
		if (decomposeAction == null || DataManager.DECOMPOSABLE_ITEMS_DATA.getSelectableItems(item.getItemId()) != null) { // exclude selectable decomposables
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_IT_CAN_NOT_BE_DECOMPOSED(item.getItemTemplate().getL10n()));
			return;
		}

		if (!decomposeAction.canAct(player, item, item))
			return;

		startTask(player, item.getItemId(), count, decomposeAction);
	}

	private void startTask(Player player, int itemId, long count, DecomposeAction decomposeAction) {
		player.getController().addTask(TaskId.SKILL_USE, ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			long remainingCount = count;
			long totalCount = 0;
			ItemUseObserver observer;

			{
				// use observer to abort task on move, attack, die, item use, etc.
				observer = new ItemUseObserver() {

					@Override
					public void itemused(Item item) {
						if (item.getItemId() != itemId)
							abort();
					}

					@Override
					public void abort() {
						cancelTask(player, observer, "Decomposing aborted: Processed " + Math.max(0, totalCount - 1) + "x " + ChatUtil.item(itemId) + ".");
					}
				};

				player.getObserveController().addObserver(observer);
			}

			@Override
			public void run() {
				Item item = player.getInventory().getFirstItemByItemId(itemId);
				remainingCount = Math.min(player.getInventory().getItemCountByItemId(itemId), remainingCount);
				if (item == null || remainingCount <= 0) {
					cancelTask(player, observer, "Decomposing finished: Processed " + totalCount + "x " + ChatUtil.item(itemId) + ".");
					return;
				}

				if (!decomposeAction.canAct(player, item, item)) {
					cancelTask(player, observer, "Decomposing aborted: Processed " + totalCount + "x " + ChatUtil.item(itemId) + ".");
					return;
				}

				player.getObserveController().notifyItemuseObservers(item); // cancel hide
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
