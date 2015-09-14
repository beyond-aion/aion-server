package admincommands;

import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.VortexService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

public class Invasion extends AdminCommand {

	private static final String COMMAND_START = "start";
	private static final String COMMAND_STOP = "stop";

	public Invasion() {
		super("invasion");
	}

	@Override
	public void execute(Player player, String... params) {

		if (params.length == 0) {
			showHelp(player);
			return;
		}

		if (COMMAND_STOP.equalsIgnoreCase(params[0]) || COMMAND_START.equalsIgnoreCase(params[0])) {
			handleStartStopInvasion(player, params);
		}
	}

	protected void handleStartStopInvasion(Player player, String... params) {
		if (params.length != 2 || !NumberUtils.isDigits(params[1])) {
			showHelp(player);
			return;
		}

		int vortexId = NumberUtils.toInt(params[1]);
		String locationName = vortexId == 0 ? "Theobomos" : "Brusthonin";
		if (!isValidVortexLocationId(player, vortexId)) {
			showHelp(player);
			return;
		}

		if (COMMAND_START.equalsIgnoreCase(params[0])) {
			if (VortexService.getInstance().isInvasionInProgress(vortexId)) {
				PacketSendUtility.sendMessage(player, locationName + " is already under siege");
			} else {
				PacketSendUtility.sendMessage(player, locationName + " invasion started!");
				VortexService.getInstance().startInvasion(vortexId);
			}
		} else if (COMMAND_STOP.equalsIgnoreCase(params[0])) {
			if (!VortexService.getInstance().isInvasionInProgress(vortexId)) {
				PacketSendUtility.sendMessage(player, locationName + " is not under siege");
			} else {
				PacketSendUtility.sendMessage(player, locationName + " invasion stopped!");
				VortexService.getInstance().stopInvasion(vortexId);
			}
		}
	}

	protected boolean isValidVortexLocationId(Player player, int vortexId) {

		if (!VortexService.getInstance().getVortexLocations().keySet().contains(vortexId)) {
			PacketSendUtility.sendMessage(player, "Id " + vortexId + " is invalid");
			return false;
		}

		return true;
	}

	protected void showHelp(Player player) {
		PacketSendUtility.sendMessage(player, "AdminCommand //invasion start|stop <Id>");
	}

}
