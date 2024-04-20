package playercommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_CHANGE_ALLOWED_HDD_SERIAL;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author ViAl, Neon
 */
public class Lock extends PlayerCommand {

	public Lock() {
		super("lock", "Enables/disables blocking logins from other computers.");

		setSyntaxInfo(
			"<enable> - Allows login from only this computer.",
			"<disable> - Allows login from any computer."
		);
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0) {
			sendInfo(player);
			return;
		}

		if ("enable".equalsIgnoreCase(params[0])) {
			String hddSerial = player.getClientConnection().getHddSerial();
			if (hddSerial == null || hddSerial.isEmpty()) {
				sendInfo(player, "Couldn't lock your account. Please re-log and try again.");
				return;
			}
			player.getAccount().setAllowedHddSerial(hddSerial);
			LoginServer.getInstance().sendPacket(new SM_CHANGE_ALLOWED_HDD_SERIAL(player.getAccount()));
			sendInfo(player, "Your account is now locked. You will not be able to login from any other computer from now on.");
		} else if ("disable".equalsIgnoreCase(params[0])) {
			player.getAccount().setAllowedHddSerial(null);
			LoginServer.getInstance().sendPacket(new SM_CHANGE_ALLOWED_HDD_SERIAL(player.getAccount()));
			sendInfo(player, "Your account is unlocked. You can login from any computer again.");
		} else {
			sendInfo(player, "Invalid parameter.");
		}
	}
}
