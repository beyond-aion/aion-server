package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemId;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Sarynth Simple admin assistance command for adding kinah to self, named player or target player. Based on
 *         //add command. Kinah Item Id - 182400001 (Using ItemId.KINAH.value())
 */
public class Kinah extends AdminCommand {

	public Kinah() {
		super("kinah");
	}

	@Override
	public void execute(Player admin, String... params) {
		long kinahCount;
		Player receiver;

		if (params.length == 1) {
			receiver = admin;
			try {
				kinahCount = Long.parseLong(params[0]);
			}
			catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "Kinah value must be an integer.");
				return;
			}
		}
		else {
			receiver = World.getInstance().findPlayer(Util.convertName(params[0]));

			if (receiver == null) {
				PacketSendUtility.sendMessage(admin, "Could not find a player by that name.");
				return;
			}

			try {
				kinahCount = Long.parseLong(params[1]);
			}
			catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "Kinah value must be an integer.");
				return;
			}
		}

		long count = ItemService.addItem(receiver, ItemId.KINAH.value(), kinahCount);

		if (count == 0) {
			PacketSendUtility.sendMessage(admin, "Kinah given successfully.");
			PacketSendUtility.sendMessage(receiver, "An admin gives you some kinah.");
		}
		else {
			PacketSendUtility.sendMessage(admin, "Kinah couldn't be given.");
		}
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //kinah [player] <quantity>");
	}
}
