package admincommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.itemset.ItemPart;
import com.aionemu.gameserver.model.templates.itemset.ItemSetTemplate;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Antivirus
 */
public class AddSet extends AdminCommand {

	public AddSet() {
		super("addset");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0 || params.length > 2) {
			onFail(player, null);
			return;
		}

		int itemSetId = 0;
		Player receiver = null;

		try {
			itemSetId = Integer.parseInt(params[0]);
			receiver = player;
		}
		catch (NumberFormatException e) {
			receiver = World.getInstance().findPlayer(Util.convertName(params[0]));

			if (receiver == null) {
				PacketSendUtility.sendMessage(player, "Could not find a player by that name.");
				return;
			}

			try {
				itemSetId = Integer.parseInt(params[1]);
			}
			catch (NumberFormatException ex) {

				PacketSendUtility.sendMessage(player, "You must give number to itemset ID.");
				return;
			}
			catch (Exception ex2) {
				PacketSendUtility.sendMessage(player, "Occurs an error.");
				return;
			}
		}

		ItemSetTemplate itemSet = DataManager.ITEM_SET_DATA.getItemSetTemplate(itemSetId);
		if (itemSet == null) {
			PacketSendUtility.sendMessage(player, "ItemSet does not exist with id " + itemSetId);
			return;
		}

		if (receiver.getInventory().getFreeSlots() < itemSet.getItempart().size()) {
			PacketSendUtility
				.sendMessage(player, "Inventory needs at least " + itemSet.getItempart().size() + " free slots.");
			return;
		}

		for (ItemPart setPart : itemSet.getItempart()) {
			long count = ItemService.addItem(receiver, setPart.getItemid(), 1);

			if (count != 0) {
				PacketSendUtility.sendMessage(player, "Item " + setPart.getItemid() + " couldn't be added");
				return;
			}
		}

		PacketSendUtility.sendMessage(player, "Item Set added successfully");
		PacketSendUtility.sendMessage(receiver, "admin gives you an item set");
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //addset <player> <itemset ID>");
		PacketSendUtility.sendMessage(player, "syntax //addset <itemset ID>");
	}

}
