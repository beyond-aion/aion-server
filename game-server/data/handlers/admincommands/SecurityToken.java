package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.player.SecurityTokenService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Artur
 */
public class SecurityToken extends AdminCommand {

	public SecurityToken() {
		super("stoken", "Manages security tokens.", """
			<player> - Generates a new security token for the online player.
			show <player> - Shows the security token of the online player.
			""");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length < 1 || (params[0].equals("show") && params.length < 2)) {
			sendInfo(player);
			return;
		}
		Player receiver = null;

		if (params[0].equals("show")) {
			receiver = World.getInstance().getPlayer(Util.convertName(params[1]));
			if (receiver == null) {
				PacketSendUtility.sendMessage(player, "Can't find this player, maybe he's not online");
				return;
			}

			if (!"".equals(receiver.getAccount().getSecurityToken())) {
				PacketSendUtility.sendMessage(player, "The Security Token of this player is: " + receiver.getAccount().getSecurityToken());
			} else {
				PacketSendUtility.sendMessage(player, "This player haven't an Security Token!");
			}

		} else {
			receiver = World.getInstance().getPlayer(Util.convertName(params[0]));

			if (receiver == null) {
				PacketSendUtility.sendMessage(player, "Can't find this player, maybe he's not online");
				return;
			}

			SecurityTokenService.generateToken(receiver.getAccount());
		}

	}

}
