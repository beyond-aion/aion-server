package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * Admin moveplayertoplayer command.
 * 
 * @author Tanelorn
 */
public class MovePlayerToPlayer extends AdminCommand {

	public MovePlayerToPlayer() {
		super("moveplayertoplayer", "Teleports a player to another one.", """
			<player> <destination player> - Teleports the first player to the second one.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params == null || params.length < 2) {
			sendInfo(admin);
			return;
		}

		Player playerToMove = World.getInstance().getPlayer(Util.convertName(params[0]));
		if (playerToMove == null) {
			sendInfo(admin, "The specified player is not online.");
			return;
		}

		Player playerDestination = World.getInstance().getPlayer(Util.convertName(params[1]));
		if (playerDestination == null) {
			sendInfo(admin, "The destination player is not online.");
			return;
		}

		if (playerToMove.equals(playerDestination)) {
			sendInfo(admin, "Cannot move the specified player to their own position.");
			return;
		}

		TeleportService.teleportTo(playerToMove, playerDestination.getWorldId(), playerDestination.getInstanceId(), playerDestination.getX(),
			playerDestination.getY(), playerDestination.getZ(), playerDestination.getHeading());

		sendInfo(admin, "Teleported player " + playerToMove.getName() + " to the location of player " + playerDestination.getName()
			+ ".");
		sendInfo(playerToMove, "You have been teleported by an administrator.");
	}
}
