package consolecommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.AdminService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Wishid extends ConsoleCommand {

	public Wishid() {
		super("wishid", "Adds items.", """
			<count> <item id> - Adds the items to your target (or yourself).
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 2) {
			sendInfo(admin);
			return;
		}

		Player target = admin;
		if (admin.getTarget() instanceof Player player) {
			target = player;
		}

		long itemCount = Long.parseLong(params[0]);
		int itemId = Integer.parseInt(params[1]);
		if (itemCount < 1) {
			sendInfo(admin, "The count must be at least 1.");
			return;
		}

		if (DataManager.ITEM_DATA.getItemTemplate(itemId) == null) {
			sendInfo(admin, "Item id is incorrect: " + itemId);
			return;
		}

		if (!AdminService.getInstance().canOperate(admin, target, itemId, "command ///wishid"))
			return;

		long count = ItemService.addItem(target, itemId, itemCount, true);

		if (count == 0) {
			if (admin != target) {
				sendInfo(admin, "You successfully gave " + itemCount + " x [item:" + itemId + "] to " + target.getName() + ".");
				sendInfo(target, "You successfully received " + itemCount + " x [item:" + itemId + "] from " + admin.getName() + ".");
			} else
				sendInfo(admin, "You successfully received " + itemCount + " x [item:" + itemId + "]");
		} else {
			sendInfo(admin, "Item couldn't be added");
		}
	}

}
