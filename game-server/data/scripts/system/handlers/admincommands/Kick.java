package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Elusive, Neon
 */
public class Kick extends AdminCommand {

	public Kick() {
		super("kick", "Disconnects players from the server.");

		// @formatter:off
		setSyntaxInfo(
			"<name> - Disconnects the player with the specified name.",
			"<ALL> - Disconnects everyone (parameter must be typed in uppercase, for safety)."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		if ("ALL".equals(params[0])) {
			if (World.getInstance().getAllPlayers().size() == 1) {
				sendInfo(admin, "There is nobody online to kick.");
				return;
			}
			World.getInstance().forEachPlayer(player -> {
				if (!player.equals(admin)) {
					player.getClientConnection().close(SM_SYSTEM_MESSAGE.STR_KICK_CHARACTER());
					PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_USER_KICKED(player.getName()));
				}
			});
		} else {
			Player player = World.getInstance().getPlayer(Util.convertName(params[0]));
			if (player == null) {
				PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_BUDDYLIST_NO_OFFLINE_CHARACTER());
				return;
			}
			player.getClientConnection().close(SM_SYSTEM_MESSAGE.STR_KICK_CHARACTER());
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_USER_KICKED(player.getName()));
		}
	}
}
