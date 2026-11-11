package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.player.SecurityTokenService;
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
				sendInfo(player, "Player " + Util.convertName(params[1]) + " is not online.");
				return;
			}

			if (!"".equals(receiver.getAccount().getSecurityToken())) {
				sendInfo(player,
					"The security token of " + receiver.getName() + " is: " + receiver.getAccount().getSecurityToken());
			} else {
				sendInfo(player, "Player " + receiver.getName() + " has no security token.");
			}

		} else {
			receiver = World.getInstance().getPlayer(Util.convertName(params[0]));

			if (receiver == null) {
				sendInfo(player, "Player " + Util.convertName(params[0]) + " is not online.");
				return;
			}

			SecurityTokenService.generateToken(receiver.getAccount());
			sendInfo(player, "A new security token was generated for " + receiver.getName() + ".");
		}

	}

}
