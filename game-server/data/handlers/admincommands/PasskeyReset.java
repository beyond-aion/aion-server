package admincommands;

import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dao.PlayerPasskeyDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author cura
 */
public class PasskeyReset extends AdminCommand {

	public PasskeyReset() {
		super("passkeyreset", "Resets an account passkey.", """
			<player> <passkey> - Sets a new 6-8 digit passkey for the account of the player.
			""");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length < 2) {
			sendInfo(player);
			return;
		}

		String name = Util.convertName(params[0]);
		int accountId = PlayerDAO.getAccountIdByName(name);
		if (accountId == 0) {
			sendInfo(player, "Player " + name + " was not found!");
			return;
		}

		String newPasskey = params[1];
		if (!newPasskey.matches("\\d{6,8}")) {
			sendInfo(player, "The passkey must consist of 6 to 8 digits.");
			return;
		}

		if (!PlayerPasskeyDAO.updateForcePlayerPasskey(accountId, newPasskey)) {
			sendInfo(player, "The account of " + name + " has no passkey to reset.");
			return;
		}
		LoginServer.getInstance().kickAccount(accountId, false);
		sendInfo(player, "The passkey of " + name + "'s account was reset.");
	}
}
