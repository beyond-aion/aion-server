package consolecommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.AdminService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Wishid extends ConsoleCommand {

	public Wishid() {
		super("wishid");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			info(admin, null);
			return;
		}

		final VisibleObject target = admin.getTarget();
		if (target == null) {
			PacketSendUtility.sendMessage(admin, "No target selected.");
			return;
		}

		if (!(target instanceof Player)) {
			PacketSendUtility.sendMessage(admin, "This command can only be used on a player!");
			return;
		}

		final Player player = (Player) target;

		long itemCount;
		int itemId;

		try {
			itemCount = Long.parseLong(params[0]);
			itemId = Integer.parseInt(params[1]);
		} catch (NumberFormatException e) {
			info(admin, null);
			return;
		}

		if (DataManager.ITEM_DATA.getItemTemplate(itemId) == null) {
			PacketSendUtility.sendMessage(admin, "Item id is incorrect: " + itemId);
			return;
		}

		if (!AdminService.getInstance().canOperate(admin, player, itemId, "command ///wishid"))
			return;

		long count = ItemService.addItem(player, itemId, itemCount, true);

		if (count == 0) {
			if (admin != player) {
				PacketSendUtility.sendMessage(admin, "You successfully gave " + itemCount + " x [item:" + itemId + "] to " + player.getName() + ".");
				PacketSendUtility.sendMessage(player, "You successfully received " + itemCount + " x [item:" + itemId + "] from " + admin.getName() + ".");
			} else
				PacketSendUtility.sendMessage(admin, "You successfully received " + itemCount + " x [item:" + itemId + "]");
		} else {
			PacketSendUtility.sendMessage(admin, "Item couldn't be added");
		}
	}

	@Override
	public void info(Player admin, String message) {
		PacketSendUtility.sendMessage(admin, "syntax ///wishid <Quantity> <item Id>");
	}

}
