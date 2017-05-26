package consolecommands;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemDeleteType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Delete_items extends ConsoleCommand {

	public Delete_items() {
		super("delete_items");
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

		int quality;

		try {
			quality = Integer.parseInt(params[0]);
		} catch (NumberFormatException e) {
			PacketSendUtility.sendMessage(admin, "Parameters need to be an integer.");
			return;
		}

		if (quality < 0 || quality >= ItemQuality.values().length) {
			PacketSendUtility.sendMessage(admin, "Invalid QualityId.");
			return;
		}

		for (Item item : player.getInventory().getItems()) {
			if (item.getItemTemplate().getItemQuality().getQualityId() <= quality) {
				player.getInventory().delete(item, ItemDeleteType.DISCARD);
			}
		}
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax ///delete_items <item quality>");
	}

}
