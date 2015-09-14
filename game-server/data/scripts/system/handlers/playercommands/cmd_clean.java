package playercommands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Source
 */
public class cmd_clean extends PlayerCommand {

	public cmd_clean() {
		super("clean");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0) {
			info(player, null);
			return;
		}

		int itemId = 0;
		long itemCount = 1;
		byte itemCountIndex = 1;
		String itemString = params[0];
		if (itemString.equals("[item:") && params.length >= 2) {
			// some item links have space before their ID
			itemString += params[1];
			if (params.length > 2) {
				itemCountIndex = 2;
			}
		}
		try {
			if (params.length > 1 && (itemCountIndex < params.length)) {
				// count parameter was passed
				itemCount = Long.parseLong(params[itemCountIndex]);
			}

			Pattern id = Pattern.compile("(?:\\[item:)??(\\d{9})");
			Matcher result = id.matcher(itemString);
			if (result.find()) {
				itemId = Integer.parseInt(result.group(1));
			}
		} catch (NumberFormatException e) {
			info(player, "Invalid number parameter passed.");
			return;
		}

		if (itemId > 0) {
			if (itemCount > 0) {
				Storage bag = player.getInventory();
				long bagItemCount = bag.getItemCountByItemId(itemId);
				if (bagItemCount >= 1) {
					if (itemCount <= bagItemCount) {
						bag.decreaseByItemId(itemId, itemCount);
						PacketSendUtility.sendMessage(player, "Successfully removed " + itemCount + "x [item:" + itemId + "] from your inventory.");
					} else {
						info(player, "You only have " + bagItemCount + ".");
					}
				} else {
					info(player, "You don't have that item.");
				}
			} else {
				info(player, "Invalid item count.");
			}
		} else {
			info(player, "Invalid item ID.");
		}
	}

	@Override
	public void info(Player player, String message) {
		if (message != null && !message.isEmpty()) {
			PacketSendUtility.sendMessage(player, message);
		}
		PacketSendUtility.sendMessage(player, "Syntax: .clean <item ID|item @link> [<count>]");
	}
}
