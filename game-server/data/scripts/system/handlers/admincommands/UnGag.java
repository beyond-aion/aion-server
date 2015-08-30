package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.ban.ChatBanService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Watson
 */
public class UnGag extends AdminCommand {

	public UnGag() {
		super("ungag");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params == null || params.length < 1) {
			PacketSendUtility.sendMessage(admin, "Syntax: //ungag <player>");
			return;
		}

		String name = Util.convertName(params[0]);
		Player player = World.getInstance().findPlayer(name);
		if (player == null) {
			PacketSendUtility.sendMessage(admin, "Player " + name + " was not found!");
			PacketSendUtility.sendMessage(admin, "Syntax: //ungag <player>");
			return;
		}

		ChatBanService.unbanPlayer(player);
		ChatBanService.deleteBan(player.getPlayerAccount().getId());

		PacketSendUtility.sendMessage(admin, "Player " + name + " ungagged");
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "Syntax: //ungag <player>");
	}
}
