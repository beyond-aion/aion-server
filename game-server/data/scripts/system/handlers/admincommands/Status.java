package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team2.group.PlayerGroupService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author KID
 */
public class Status extends AdminCommand {

	public Status() {
		super("status");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params[0].equalsIgnoreCase("alliance")) {
			PacketSendUtility.sendMessage(admin, PlayerAllianceService.getServiceStatus());
		}
		else if (params[0].equalsIgnoreCase("group")) {
			PacketSendUtility.sendMessage(admin, PlayerGroupService.getServiceStatus());
		}
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "<usage //status alliance | group");
	}
}
