package admincommands;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
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
		super("res", "Resurrects your target.", """
			[prompt] - Asks the targeted dead player to resurrect (default).
			instant - Resurrects the targeted dead player instantly.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		final VisibleObject target = admin.getTarget();
		if (target == null) {
			sendInfo(admin, "No target selected.");
			return;
		}

		if (!(target instanceof Player)) {
			sendInfo(admin, "You can only resurrect other players.");
			return;
		}

		final Player player = (Player) target;
		if (!player.isDead()) {
			sendInfo(admin, "That player is already alive.");
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

		sendInfo(admin);
	}
}
