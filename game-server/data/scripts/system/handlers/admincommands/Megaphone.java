package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MEGAPHONE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ginho1
 */
public class Megaphone extends AdminCommand {

	public Megaphone() {
		super("megaphone");
	}

	@Override
	public void execute(Player player, String... params) {

		if (params.length < 1) {
			info(player, null);
			return;
		}

		String message = "";

		// Add with space
		for (int i = 0; i < params.length - 1; i++)
			message += params[i] + " ";

		// Add the last without the end space
		message += params[params.length - 1];

		PacketSendUtility.broadcastToWorld(new SM_MEGAPHONE(player.getName(), message, 188910000));
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "Syntax: //megaphone <message>");
	}
}
