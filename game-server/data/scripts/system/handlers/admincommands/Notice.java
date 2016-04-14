package admincommands;

import java.util.Iterator;

import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * Admin notice command
 * 
 * @author Jenose Updated By Darkwolf
 */
public class Notice extends AdminCommand {

	public Notice() {
		super("notice");
	}

	@Override
	public void execute(Player player, String... params) {

		String message = "";

		try {
			for (int i = 0; i < params.length; i++) {
				message += " " + params[i];
			}
		} catch (NumberFormatException e) {
			PacketSendUtility.sendMessage(player, "Parameters should be text or number !");
			return;
		}
		Iterator<Player> iter = World.getInstance().getPlayersIterator();

		while (iter.hasNext()) {
			PacketSendUtility.sendMessage(iter.next(), "Information: " + message, ChatType.BRIGHT_YELLOW_CENTER);
		}
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "Syntax: //notice <message>");
	}
}
