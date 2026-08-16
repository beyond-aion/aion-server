package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RESURRECT;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Sarynth
 */
public class Res extends AdminCommand {

	public Res() {
		super("res");
	}

	@Override
	public void execute(Player admin, String... params) {
		final Player player = admin.getTarget() instanceof Player target ? target : admin;
		if (!player.isDead()) {
			PacketSendUtility.sendMessage(admin, player.equals(admin) ? "You're already alive." : player.getName() + " is already alive.");
			return;
		}

		// Default action is to prompt for resurrect.
		if (params == null || params.length == 0 || ("prompt").startsWith(params[0])) {
			player.setPlayerResActivate(true);
			PacketSendUtility.sendPacket(player, new SM_RESURRECT(admin));
			return;
		}

		if (("instant").startsWith(params[0])) {
			PlayerReviveService.skillRevive(player);
			return;
		}

		PacketSendUtility.sendMessage(admin, "[Resurrect] Usage: target player and use //res <instant|prompt>");
	}

	@Override
	public void info(Player player, String message) {
		// TODO Auto-generated method stub
	}
}
