package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Watson
 */
public class BanIp extends AdminCommand {

	public BanIp() {
		super("banip", "Bans an IP mask.", "<mask> [minutes] - Bans the IP mask for the given time (default: permanent).");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params == null || params.length < 1) {
			sendInfo(player);
			return;
		}

		String mask = params[0];

		int time = 0; // Default: infinity
		if (params.length > 1)
			time = Integer.parseInt(params[1]);
		if (time == 0) {
			time = 60 * 24 * 365 * 10;// pseudo infinity
		}

		LoginServer.getInstance().sendBanPacket((byte) 2, 0, mask, time, player.getObjectId());
	}
}
