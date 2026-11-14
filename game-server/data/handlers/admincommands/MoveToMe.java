package admincommands;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Cyrakuse, Estrayl
 */
public class MoveToMe extends AdminCommand {

	public MoveToMe() {
		super("movetome", "Teleports a player (optional his team) to the user.", """
			<name> - Teleports only the player.
			<name> <(g)rp|(a)lli> - Teleports either the players group or his alliance including him.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			sendInfo(admin);
			return;
		}
		String playerName = Util.convertName(params[0]);
		Player playerToMove = World.getInstance().getPlayer(playerName);
		if (playerToMove == null) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(playerName));
			return;
		}
		if (params.length >= 2) {
			TemporaryPlayerTeam<?> teamToMove;
			switch (params[1].toLowerCase()) {
				case "g":
				case "grp":
				case "group":
					teamToMove = playerToMove.getCurrentGroup();
					break;
				case "a":
				case "alli":
				case "alliance":
					teamToMove = playerToMove.getCurrentTeam();
					break;
				default:
					sendInfo(admin);
					return;
			}
			if (teamToMove == null) {
				sendInfo(admin, name(playerToMove) + " currently has no team.");
				return;
			}
			teamToMove.getOnlineMembers().forEach(p -> teleportPlayer(p, admin));
		} else {
			teleportPlayer(playerToMove, admin);
		}
	}

	public static void teleportPlayer(Player playerToMove, Player admin) {
		if (playerToMove.equals(admin))
			return;
		TeleportService.teleportTo(playerToMove, admin.getWorldMapInstance(), admin.getX(), admin.getY(), admin.getZ(), admin.getHeading(),
			TeleportAnimation.FADE_OUT_BEAM);
		sendInfo(admin, "Teleported " + name(playerToMove) + " to your location.");
		sendInfo(playerToMove, "You have been teleported by " + name(admin) + ".");
	}
}
