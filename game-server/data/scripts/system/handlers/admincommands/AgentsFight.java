package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.agentsfight.AgentsFightService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Yeats
 *
 */
public class AgentsFight extends AdminCommand {

	public AgentsFight() {
		super("agents");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length < 1) {
			onFail(player, null);
			return;
		}
		if (params[0].equalsIgnoreCase("start")) {
			if (AgentsFightService.getInstance().isStarted()) {
				PacketSendUtility.sendMessage(player, "Agents Fight is already running!");
			} else {
				AgentsFightService.getInstance().onStart();
				PacketSendUtility.sendMessage(player, "Started Agents Fight!");
			}
		} else if (params[0].equalsIgnoreCase("stop")) {
			if (AgentsFightService.getInstance().isStarted()) {
				AgentsFightService.getInstance().stop(null);
				PacketSendUtility.sendMessage(player, "Stopped Agents Fight!");
			} else {
				PacketSendUtility.sendMessage(player, "Agents Fight is not running.");
			}
		} else {
			onFail(player, null);
		}
	}
	
	@Override
	public void onFail(Player player, String msg) {
		PacketSendUtility.sendMessage(player, "Syntax: //agents <start | stop>");
	}
}
