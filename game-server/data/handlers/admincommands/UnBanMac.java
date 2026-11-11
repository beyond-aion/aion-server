package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.BannedMacManager;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author KID
 */
public class UnBanMac extends AdminCommand {

	public UnBanMac() {
		super("unbanmac", "Removes a MAC ban.", """
			<mac> - Unbans the MAC address.
			""");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params == null || params.length < 1) {
			sendInfo(player);
			return;
		}

		String address = params[0];
		boolean result = BannedMacManager.getInstance().unbanAddress(address,
			"uban;mac=" + address + ", " + player.getObjectId() + "; admin=" + player.getName());
		if (result)
			sendInfo(player, "MAC " + address + " has been unbanned.");
		else
			sendInfo(player, "MAC " + address + " is not banned.");
	}
}
