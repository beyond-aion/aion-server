package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Watson
 */
public class UnBanIp extends AdminCommand {

	public UnBanIp() {
		super("unbanip");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params == null || params.length < 1) {
			PacketSendUtility.sendMessage(player, "Syntax: //unbanip <mask>");
			return;
		}

		LoginServer.getInstance().sendBanPacket((byte) 2, 0, params[0], -1, player.getObjectId());
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "Syntax: //unbanip <mask>");
	}
}
