package admincommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.itemset.ItemPart;
import com.aionemu.gameserver.model.templates.itemset.ItemSetTemplate;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Antivirus
 */
public class AddSet extends AdminCommand {

	public AddSet() {
		super("addset", "Adds an item set.", """
			<itemset ID> - Adds all items of the set to yourself.
			<player> <itemset ID> - Adds all items of the set to the online player.
			""");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0 || params.length > 2) {
			sendInfo(player);
			return;
		}

		Player receiver = player;
		if (params.length == 2) {
			receiver = World.getInstance().getPlayer(Util.convertName(params[0]));
			if (receiver == null) {
				sendInfo(player, "Player " + Util.convertName(params[0]) + " is not online.");
				return;
			}
		}
		int itemSetId = Integer.parseInt(params[params.length - 1]);

		ItemSetTemplate itemSet = DataManager.ITEM_SET_DATA.getItemSetTemplate(itemSetId);
		if (itemSet == null) {
			sendInfo(player, "ItemSet does not exist with id " + itemSetId);
			return;
		}

		if (receiver.getInventory().getFreeSlots() < itemSet.getItempart().size()) {
			sendInfo(player, "Inventory needs at least " + itemSet.getItempart().size() + " free slots.");
			return;
		}

		for (ItemPart setPart : itemSet.getItempart()) {
			long count = ItemService.addItem(receiver, setPart.getItemId(), 1);

			if (count != 0) {
				sendInfo(player, "Item " + setPart.getItemId() + " couldn't be added");
				return;
			}
		}

		sendInfo(player, "Item set added successfully.");
		if (receiver != player)
			sendInfo(receiver, "An admin gave you an item set.");
	}

}
