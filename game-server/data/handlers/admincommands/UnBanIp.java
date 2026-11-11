package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Watson
 */
public class UnBanIp extends AdminCommand {

	public UnBanIp() {
		super("unbanip", "Removes an IP ban.", """
			<mask> - Unbans the IP mask.
			""");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params == null || params.length < 1) {
			sendInfo(player);
			return;
		}

		LoginServer.getInstance().sendBanPacket((byte) 2, 0, params[0], -1, player.getObjectId());
	}
}
