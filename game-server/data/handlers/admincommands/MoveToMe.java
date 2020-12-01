package admincommands;

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
		super("movetome", "Teleports a player (optional his team) to the user.");
		// @formatter:off
		setSyntaxInfo(
			"<name> - Teleports only the player.",
			"<name> <(g)rp|(a)lli> - Teleports either the players group or his alliance including him.");
		// @formatter:on
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
			if (!playerToMove.isInTeam()) {
				sendInfo(admin, "The player does not belong to a team.");
				return;
			}
			TemporaryPlayerTeam<?> teamToMove;
			switch (params[1].toLowerCase()) {
				case "g":
				case "grp":
				case "group":
					teamToMove = playerToMove.getPlayerGroup();
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
				sendInfo(admin, playerToMove.getName() + " currently has no team.");
				return;
			}
			teamToMove.getOnlineMembers().forEach(p -> teleportPlayer(p, admin));
		} else {
			teleportPlayer(playerToMove, admin);
		}
	}

	private void teleportPlayer(Player playerToMove, Player admin) {
		TeleportService.teleportTo(playerToMove, admin.getPosition());
		sendInfo(admin, "Teleported " + playerToMove.getName() + " to your location.");
		sendInfo(playerToMove, "You have been teleported by " + admin.getName() + ".");
	}
}
