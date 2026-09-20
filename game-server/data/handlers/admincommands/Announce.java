package admincommands;

import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Neon
 */
public class Announce extends AdminCommand {

	public Announce() {
		super("announce", "Sends a server-wide notice.", """
			n <message> - Sends the message with your name.
			a <message> - Sends the message anonymously.
			ely <message> - Sends an anonymous message to all Elyos players.
			asmo <message> - Sends an anonymous message to all Asmodian players.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length <= 1) {
			sendInfo(admin);
			return;
		}
		String message;
		Race allowedRace = null;
		if ("n".equalsIgnoreCase(params[0])) {
			message = name(admin) + ": ";
		} else if ("a".equalsIgnoreCase(params[0])) {
			message = "Announce: ";
		} else if ("ely".equalsIgnoreCase(params[0])) {
			message = "Elyos: ";
			allowedRace = Race.ELYOS;
		} else if ("asmo".equalsIgnoreCase(params[0])) {
			message = "Asmodians: ";
			allowedRace = Race.ASMODIANS;
		} else {
			sendInfo(admin);
			return;
		}
		message += join(params, 1);
		for (Player player : World.getInstance().getAllPlayers())
			if (allowedRace == null || player.getRace() == allowedRace || validateAccess(player))
				PacketSendUtility.sendMessage(player, message, ChatType.BRIGHT_YELLOW_CENTER);
	}
}
