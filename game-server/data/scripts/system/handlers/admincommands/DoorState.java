package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.StaticDoorService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Rolandas
 */
public class DoorState extends AdminCommand {

	public DoorState() {
		super("doorstate");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length != 3) {
			onFail(admin, null);
			return;
		}

		int doorId = 0;
		try {
			doorId = Integer.parseInt(params[0]);
		}
		catch (NumberFormatException e) {
			PacketSendUtility.sendMessage(admin, "<id> must be a number!");
			return;
		}

		Boolean open = null;
		if (params[1].equalsIgnoreCase("open")) {
			open = true;
		}
		else if (params[1].equalsIgnoreCase("close")) {
			open = false;
		}
		if (open == null) {
			onFail(admin, null);
			return;
		}

		int state = 0;
		try {
			state = Integer.parseInt(params[2]);
		}
		catch (NumberFormatException e) {
			PacketSendUtility.sendMessage(admin, "<state> must be a number!");
			return;
		}

		StaticDoorService.getInstance().changeStaticDoorState(admin, doorId, open, state);
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "<usage //doorstate <id> <open|close> <state>");
	}
}
