package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.PunishmentService;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author lord_rex Command: //sprison <player> <delay>(minutes) This command is sending player to prison.
 */
public class SPrison extends AdminCommand {

	public SPrison() {
		super("sprison", "Sends a player to prison.", """
			<player> <minutes> <reason> - Sends the online player to prison for the given time.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 3) {
			sendInfo(admin);
			return;
		}

		Player playerToPrison = World.getInstance().getPlayer(Util.convertName(params[0]));
		if (playerToPrison == null) {
			sendInfo(admin, "Player " + Util.convertName(params[0]) + " is not online.");
			return;
		}
		int delay = Integer.parseInt(params[1]);
		if (delay <= 0) {
			sendInfo(admin, "The time must be at least one minute.");
			return;
		}
		String reason = join(params, 2);
		PunishmentService.setIsInPrison(playerToPrison, true, delay, reason);
		sendInfo(admin,
			"Player " + playerToPrison.getName() + " was sent to prison for " + delay + " minutes because " + reason + ".");
	}
}
