package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Watson
 */
public class BanIp extends AdminCommand {

	public BanIp() {
		super("banip", "Bans an IP mask.", """
			<mask> [minutes] - Bans the IP mask for the given time (default: permanent).
			""");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params == null || params.length < 1) {
			sendInfo(player);
			return;
		}

		String mask = params[0];

		int time = params.length > 1 ? Integer.parseInt(params[1]) : 0; // 0 is permanent, the login server treats negative values as an unban
		if (time < 0) {
			sendInfo(player, "The time must not be negative.");
			return;
		}

		LoginServer.getInstance().sendBanPacket((byte) 2, 0, mask, time, player.getObjectId());
	}
}
