package admincommands;

import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Watson
 */
public class Ban extends AdminCommand {

	public Ban() {
		super("ban", "Bans an account.", """
			<player> [account|ip|full] [minutes] - Bans the player's account (default: full, permanent).
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params == null || params.length < 1) {
			sendInfo(admin);
			return;
		}

		// We need to get player's account ID
		String name = Util.convertName(params[0]);
		int accountId = 0;
		String accountIp = "";

		// First, try to find player in the World
		Player player = World.getInstance().getPlayer(name);
		if (player != null) {
			accountId = player.getClientConnection().getAccount().getId();
			accountIp = player.getClientConnection().getIP();
		}

		// Second, try to get account ID of offline player from database
		if (accountId == 0)
			accountId = PlayerDAO.getAccountIdByName(name);

		// Third, fail
		if (accountId == 0) {
			sendInfo(admin, "Player " + name + " was not found!");
			return;
		}

		byte type = 3; // Default: full
		if (params.length > 1) {
			// Smart Matching
			String stype = params[1].toLowerCase();
			if (("account").startsWith(stype))
				type = 1;
			else if (("ip").startsWith(stype))
				type = 2;
			else if (("full").startsWith(stype))
				type = 3;
			else {
				sendInfo(admin);
				return;
			}
		}

		int time = params.length > 2 ? Integer.parseInt(params[2]) : 0; // 0 is permanent, the login server treats negative values as an unban
		if (time < 0) {
			sendInfo(admin, "The time must not be negative.");
			return;
		}

		LoginServer.getInstance().sendBanPacket(type, accountId, accountIp, time, admin.getObjectId());
	}
}
