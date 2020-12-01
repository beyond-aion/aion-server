package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Watson
 */
public class BanIp extends AdminCommand {

	public BanIp() {
		super("banip");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params == null || params.length < 1) {
			PacketSendUtility.sendMessage(player, "Syntax: //banip <mask> [time in minutes]");
			return;
		}

		String mask = params[0];

		int time = 0; // Default: infinity
		if (params.length > 1) {
			try {
				time = Integer.parseInt(params[1]);
			} catch (NumberFormatException e) {
				info(player, e.getMessage());
				return;
			}
		}
		if (time == 0) {
			time = 60 * 24 * 365 * 10;// pseudo infinity
		}

		LoginServer.getInstance().sendBanPacket((byte) 2, 0, mask, time, player.getObjectId());
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "Syntax: //banip <mask> [time in minutes]");
	}
}
