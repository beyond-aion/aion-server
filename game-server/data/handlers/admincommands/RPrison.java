package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.PunishmentService;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author lord_rex Command: //rprison <player> This command is removing player from prison.
 */
public class RPrison extends AdminCommand {

	public RPrison() {
		super("rprison", "Releases a player from prison.", """
			<player> - Releases the player from prison.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length != 1) {
			sendInfo(admin);
			return;
		}

		Player playerFromPrison = World.getInstance().getPlayer(Util.convertName(params[0]));
		if (playerFromPrison == null) {
			sendInfo(admin, "Player " + Util.convertName(params[0]) + " is not online.");
			return;
		}
		if (!playerFromPrison.isInPrison()) {
			sendInfo(admin, "Player " + playerFromPrison.getName() + " is not in prison.");
			return;
		}
		PunishmentService.setIsInPrison(playerFromPrison, false, 0, "");
		sendInfo(admin, "Player " + playerFromPrison.getName() + " was released from prison.");
	}
}
