package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author Yeats.
 */
public class Inventory extends ConsoleCommand {

	public Inventory() {
		super("inventory");
	}
	@Override
	protected void execute(Player player, String... params) {

		return;
		/*
		if (player.getTarget() instanceof Player) {
			Player target = (Player) player.getTarget();
			target.setCubeLimit();
			target.setWarehouseLimit();
			// items
			Storage inventory = target.getInventory();
			List<Item> allItems = new ArrayList<>();
			if (inventory.getKinah() == 0) {
				inventory.increaseKinah(0); // create an empty object with value 0
			}
			allItems.add(inventory.getKinahItem()); // always included even with 0 count, and first in the packet !
			allItems.addAll(target.getEquipment().getEquippedItems());
			allItems.addAll(inventory.getItems());

			ListSplitter<Item> splitter = new ListSplitter<>(allItems, 10, true);
			while (splitter.hasMore()) {
				PacketSendUtility.sendPacket(player, new SM_GM_SHOW_PLAYER_SKILLS(splitter.isFirst(), splitter.getNext(), target));
			}
			PacketSendUtility.sendPacket(player, new SM_GM_SHOW_PLAYER_SKILLS(false, new ArrayList<>(), target));
		}
		*/
	}
}
