package playercommands;

import java.util.concurrent.atomic.AtomicLong;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.actions.DecomposeAction;
import com.aionemu.gameserver.model.templates.item.actions.ItemActions;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.restrictions.PlayerRestrictions;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Neon
 */
public class Decompose extends PlayerCommand {

	public Decompose() {
		super("decompose", "Opens decomposable items.", """
			<item> [count] - Decomposes the specified item (default: all, optional: number of items to decompose).
			""");
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
		if (!PlayerRestrictions.canUseItem(player, item))
			return;
		ItemActions itemActions = item.getItemTemplate().getActions();
		DecomposeAction decomposeAction = itemActions == null ? null
			: itemActions.getItemActions().stream().filter(a -> a instanceof DecomposeAction).map(DecomposeAction.class::cast).findAny().orElse(null);
		if (decomposeAction == null || DataManager.DECOMPOSABLE_ITEMS_DATA.getSelectableItems(item.getItemId()) != null) { // exclude selectable decomposables
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_IT_CAN_NOT_BE_DECOMPOSED(item.getItemTemplate().getL10n()));
			return;
		}

		if (!decomposeAction.canAct(player, item, item))
			return;

		player.getObserveController().notifyItemuseObservers(item); // cancel hide
		startTask(player, item.getItemId(), count, decomposeAction);
	}

	private void startTask(Player player, int itemId, long count, DecomposeAction decomposeAction) {
		AtomicLong processedCount = new AtomicLong(-1); // must start at -1 because decomposeAction.act() finishes after the item's casting delay
		ItemUseObserver observer = new ItemUseObserver(player) {

			@Override
			protected void onAbort() {
				cancelTask(player, this, "Decomposing aborted: Processed " + Math.max(0, processedCount.get()) + "x " + ChatUtil.item(itemId) + ".");
			}
		};
		player.getController().addTask(TaskId.SKILL_USE, ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			Item item = player.getInventory().getFirstItemByItemId(itemId);
			if (processedCount.incrementAndGet() >= count || item == null || item.getItemCount() <= 0) {
				cancelTask(player, observer, "Decomposing finished: Processed " + processedCount + "x " + ChatUtil.item(itemId) + ".");
				return;
			}
			if (!decomposeAction.canAct(player, item, item)) {
				observer.abort();
				return;
			}
			decomposeAction.act(player, item, item);
		}, 10, DataManager.ITEM_DATA.getItemTemplate(itemId).getCastingDelay() + 100));
		player.getObserveController().addObserver(observer);
	}

	private void cancelTask(Player player, ItemUseObserver observer, String message) {
		player.getController().cancelTask(TaskId.SKILL_USE);
		player.getObserveController().removeObserver(observer);
		sendInfo(player, message);
	}
}
