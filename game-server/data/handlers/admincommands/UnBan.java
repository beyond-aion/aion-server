package admincommands;

import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Watson
 */
public class UnBan extends AdminCommand {

	public UnBan() {
		super("unban", "Removes an account ban.", """
			<player> [account|ip|full] - Removes the ban of the player's account (default: full).
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params == null || params.length < 1) {
			sendInfo(admin);
			return;
		}

		// Banned player must be offline, so get his account ID from database
		String name = Util.convertName(params[0]);
		int accountId = PlayerDAO.getAccountIdByName(name);
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

		// Sends time -1 to unban
		LoginServer.getInstance().sendBanPacket(type, accountId, "", -1, admin.getObjectId());
	}
}
