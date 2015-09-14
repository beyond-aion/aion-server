package admincommands;

import java.util.Iterator;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;
import com.aionemu.gameserver.world.World;

/**
 * Admin announce faction
 * 
 * @author Divinity
 */
public class AnnounceFaction extends AdminCommand {

	public AnnounceFaction() {
		super("announcefaction");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length < 2) {
			info(player, null);
		} else {
			String message = null;

			if (params[0].equals("ely"))
				message = "Elyos: ";
			else if (params[0].equals("asmo"))
				message = "Asmodians: ";
			else {
				info(player, null);
				return;
			}

			// Add with space
			for (int i = 1; i < params.length - 1; i++)
				message += params[i] + " ";

			// Add the last without the end space
			message += params[params.length - 1];

			Player target = null;
			Iterator<Player> iter = World.getInstance().getPlayersIterator();

			while (iter.hasNext()) {
				target = iter.next();

				if (ChatProcessor.getInstance().isCommandAllowed(target, this.getAlias()) || (params[0].equals("ely") && target.getRace() == Race.ELYOS)
					|| (params[0].equals("asmo") && target.getRace() == Race.ASMODIANS))
					PacketSendUtility.sendBrightYellowMessageOnCenter(target, message);
			}
		}
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "Syntax: //announcefaction <ely | asmo> <message>");
	}
}
