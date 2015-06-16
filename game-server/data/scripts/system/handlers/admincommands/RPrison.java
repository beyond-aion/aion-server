package admincommands;

import java.util.NoSuchElementException;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.PunishmentService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author lord_rex Command: //rprison <player> This command is removing player from prison.
 */
public class RPrison extends AdminCommand {

	public RPrison() {
		super("rprison");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0 || params.length > 2) {
			PacketSendUtility.sendMessage(admin, "syntax //rprison <player>");
			return;
		}

		try {
			Player playerFromPrison = World.getInstance().findPlayer(Util.convertName(params[0]));

			if (playerFromPrison != null) {
				PunishmentService.setIsInPrison(playerFromPrison, false, 0, "");
				PacketSendUtility.sendMessage(admin, "Player " + playerFromPrison.getName() + " removed from prison.");
			}
		}
		catch (NoSuchElementException nsee) {
			PacketSendUtility.sendMessage(admin, "Usage: //rprison <player>");
		}
		catch (Exception e) {
			PacketSendUtility.sendMessage(admin, "Usage: //rprison <player>");
		}
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //rprison <player>");
	}
}
