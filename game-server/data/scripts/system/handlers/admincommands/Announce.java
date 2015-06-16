package admincommands;

import java.util.Iterator;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Ben, Ritsu Smart Matching Enabled //announce anon This will work. as well as //announce a This will work.
 *         Both will match the "a" or "anon" to the "anonymous" flag.
 */
public class Announce extends AdminCommand {

	public Announce() {
		super("announce");
	}

	@Override
	public void execute(Player player, String... params) {
		String message;

		if (("anonymous").startsWith(params[0].toLowerCase())) {
			message = "Announce: ";
		}
		else if (("name").startsWith(params[0].toLowerCase())) {
			message = player.getName() + ": ";
		}
		else {
			PacketSendUtility.sendMessage(player, "Syntax: //announce <anonymous|name> <message>");
			return;
		}

		// Add with space
		for (int i = 1; i < params.length - 1; i++)
			message += params[i] + " ";

		// Add the last without the end space
		message += params[params.length - 1];

		Iterator<Player> iter = World.getInstance().getPlayersIterator();

		while (iter.hasNext()) {
			PacketSendUtility.sendBrightYellowMessageOnCenter(iter.next(), message);
		}
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "Syntax: //announce <anonymous|name> <message>");
	}
}
