package playercommands;

import com.aionemu.gameserver.model.Wedding;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.WeddingService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author synchro2
 */
public class cmd_answer extends PlayerCommand {

	public cmd_answer() {
		super("answer");
	}

	@Override
	public void execute(Player player, String... params) {
		Wedding wedding = WeddingService.getInstance().getWedding(player);

		if (params == null || params.length != 1) {
			PacketSendUtility.sendMessage(player, "syntax .answer yes/no.");
			return;
		}
		
		if (player.getWorldId() == 510010000 || player.getWorldId() == 520010000) {
			PacketSendUtility.sendMessage(player, "You can't use this command on prison.");
			return;
		}

		if (wedding == null) {
			PacketSendUtility.sendMessage(player, "Wedding not started.");
		}

		if (params[0].toLowerCase().equals("yes")) {
			PacketSendUtility.sendMessage(player, "You accept.");
			WeddingService.getInstance().acceptWedding(player);
		}

		if (params[0].toLowerCase().equals("no")) {
			PacketSendUtility.sendMessage(player, "You decide.");
			WeddingService.getInstance().cancelWedding(player);
		}

	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax .answer yes/no.");
	}
}