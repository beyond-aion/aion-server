package admincommands;

import java.util.Iterator;

import com.aionemu.gameserver.configs.administration.CommandsConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
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
			PacketSendUtility.sendMessage(player, "Syntax: //announcefaction <ely | asmo> <message>");
		}
		else {
			Iterator<Player> iter = World.getInstance().getPlayersIterator();
			String message = null;

			if (params[0].equals("ely"))
				message = "Elyos : ";
			else
				message = "Asmodians : ";

			// Add with space
			for (int i = 1; i < params.length - 1; i++)
				message += params[i] + " ";

			// Add the last without the end space
			message += params[params.length - 1];

			Player target = null;

			while (iter.hasNext()) {
				target = iter.next();

				if (target.getAccessLevel() > CommandsConfig.ANNONCEFACTION || target.getRace() == Race.ELYOS
					&& params[0].equals("ely"))
					PacketSendUtility.sendBrightYellowMessageOnCenter(target, message);
				else if (target.getAccessLevel() > CommandsConfig.ANNONCEFACTION
					|| target.getCommonData().getRace() == Race.ASMODIANS && params[0].equals("asmo"))
					PacketSendUtility.sendBrightYellowMessageOnCenter(target, message);
			}
		}
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "Syntax: //announcefaction <ely | asmo> <message>");
	}
}
