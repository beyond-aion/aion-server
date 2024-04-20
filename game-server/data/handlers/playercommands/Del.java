package playercommands;

import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Source, Neon
 */
public class Del extends PlayerCommand {

	public Del() {
		super("del", "Deletes items from your inventory.");

		setSyntaxInfo("<item link|ID> [count] - Removes item(s) with the specified name/ID (default: 1, optional: number of items to delete).");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0) {
			sendInfo(player);
			return;
		}

		int itemId = ChatUtil.getItemId(params[0]);
		if (itemId == 0) {
			sendInfo(player, "Invalid item.");
			return;
		}

		int itemCount = params.length > 1 ? NumberUtils.toInt(params[1]) : 1;
		if (itemCount == 0) {
			sendInfo(player, "Invalid item count.");
			return;
		}

		Storage inv = player.getInventory();
		long invCount = inv.getItemCountByItemId(itemId);
		if (invCount == 0) {
			sendInfo(player, "You don't have that item.");
			return;
		}
		if (itemCount > invCount) {
			sendInfo(player, "You only have " + invCount + ".");
			return;
		}

		inv.decreaseByItemId(itemId, itemCount);
		sendInfo(player, "Deleted " + itemCount + "x " + ChatUtil.item(itemId) + " from your inventory.");
	}
}
