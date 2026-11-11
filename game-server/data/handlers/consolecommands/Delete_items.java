package consolecommands;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemDeleteType;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Delete_items extends ConsoleCommand {

	public Delete_items() {
		super("delete_items", "Deletes inventory items.", """
			<item quality> - Deletes items up to the quality ID from your target's (or your) inventory.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			sendInfo(admin);
			return;
		}

		final Player player = admin.getTarget() instanceof Player target ? target : admin;

		int quality = Integer.parseInt(params[0]);

		if (quality < 0 || quality >= ItemQuality.values().length) {
			sendInfo(admin, "The quality ID must be between 0 and " + (ItemQuality.values().length - 1) + ".");
			return;
		}

		for (Item item : player.getInventory().getItems()) {
			if (!item.getItemTemplate().isKinah() && item.getItemTemplate().getItemQuality().getQualityId() <= quality) {
				player.getInventory().delete(item, ItemDeleteType.DISCARD);
			}
		}
	}

}
