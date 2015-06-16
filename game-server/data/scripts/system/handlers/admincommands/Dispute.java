package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.DisputeLandService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

public class Dispute extends AdminCommand {

	private static final String COMMAND_START = "start";
	private static final String COMMAND_STOP = "stop";

	public Dispute() {
		super("dispute");
	}

	@Override
	public void execute(Player player, String... params) {

		if (params.length == 0) {
			showHelp(player);
			return;
		}

		if (COMMAND_STOP.equalsIgnoreCase(params[0]) || COMMAND_START.equalsIgnoreCase(params[0])) {
			handleRift(player, params);
		}
	}

	protected void handleRift(Player player, String... params) {
		if (params.length != 1) {
			showHelp(player);
			return;
		}

		if (COMMAND_START.equalsIgnoreCase(params[0])) {
			DisputeLandService.getInstance().setActive(true);
		}
		else if (COMMAND_STOP.equalsIgnoreCase(params[0])) {
			DisputeLandService.getInstance().setActive(false);
		}
	}

	protected void showHelp(Player player) {
		PacketSendUtility.sendMessage(player, "AdminCommand //dispute start|stop");
	}

}