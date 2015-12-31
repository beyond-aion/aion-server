package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionInstance;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Yeats
 *
 */
public class Ahserion extends AdminCommand {

	public Ahserion() {
		super("ahserion");
	}

	@Override
	protected void execute(Player player, String... params) {
		if (params.length < 1) {
			return;
		}
		if (params[0].equalsIgnoreCase("start")) {
			if (!AhserionInstance.getInstance().isStarted()) {
				AhserionInstance.getInstance().start();
				PacketSendUtility.sendMessage(player, "Started Ahserion's Flight.");
			} else {
				PacketSendUtility.sendMessage(player, "Ahserion's Flight is already running.");
			}
		} else if (params[0].equalsIgnoreCase("stop")) {
			if (AhserionInstance.getInstance().isStarted()) {
				AhserionInstance.getInstance().onStop();
				PacketSendUtility.sendMessage(player, "Stopped Ahserion's Flight.");
			} else {
				PacketSendUtility.sendMessage(player, "Ahserion's Flight is not running.");
			}
		}
	}

}
