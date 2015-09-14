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
public class Gag extends AdminCommand {

	public Gag() {
		super("gag");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params == null || params.length < 1) {
			PacketSendUtility.sendMessage(admin, "Syntax: //gag <player> [time in minutes]");
			return;
		}

		String name = Util.convertName(params[0]);
		final Player player = World.getInstance().findPlayer(name);
		if (player == null) {
			PacketSendUtility.sendMessage(admin, "Player " + name + " was not found!");
			PacketSendUtility.sendMessage(admin, "Syntax: //gag <player> [time in minutes]");
			return;
		}

		int time = 0;
		if (params.length > 1) {
			try {
				time = Integer.parseInt(params[1]);
			} catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "Syntax: //gag <player> [time in minutes]");
				return;
			}
		}

		ChatBanService.banPlayer(player, time * 60000L);
		long expireTime = System.currentTimeMillis() + time * 60000L;
		ChatBanService.saveBan(player.getPlayerAccount().getId(), expireTime);

		PacketSendUtility.sendMessage(admin, "Player " + name + " gagged" + (time != 0 ? " for " + time + " minutes" : ""));
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "Syntax: //gag <player> [time in minutes]");
	}
}
