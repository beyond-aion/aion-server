package admincommands;

import java.util.concurrent.TimeUnit;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.BannedMacManager;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author KID, nrg
 */
public class BanMac extends AdminCommand {

	public BanMac() {
		super("banmac", "Bans a MAC address.", """
			<minutes> [mac] - Bans the MAC (default: your target's, kicking it), 0 minutes is permanent.
			""");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params == null || params.length < 1) {
			sendInfo(player);
			return;
		}

		int time;
		String address;
		String targetName = "direct_type";

		time = Integer.parseInt(params[0]);
		if (time < 0) {
			sendInfo(player, "The time must not be negative.");
			return;
		}
		if (time == 0) // 0 is 10 years since system don't allow infinte banns without rework - it's pseudo infinity
			time = 60 * 24 * 365 * 10;

		// is mac defined?
		if (params.length > 1) {
			address = params[1];
		} else { // no address defined
			VisibleObject target = player.getTarget();
			if (target instanceof Player) {
				if (target.equals(player)) {
					sendInfo(player, "You cannot ban your own MAC address.");
					return;
				}

				Player targetpl = (Player) target;
				address = targetpl.getClientConnection().getMacAddress();
				targetName = targetpl.getName();
				targetpl.getClientConnection().close();
			} else {
				sendInfo(player, "Select a player or specify a MAC address.");
				return;
			}
		}

		BannedMacManager.getInstance().banAddress(address, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(time),
			"author=" + player.getName() + ", " + player.getObjectId() + "; target=" + targetName);
	}

}
