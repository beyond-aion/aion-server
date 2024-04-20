package admincommands;

import org.apache.commons.lang3.StringUtils;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.ban.ChatBanService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Watson, Neon
 */
public class Gag extends AdminCommand {

	public Gag() {
		super("gag", "Bans a player from all chats.");

		setSyntaxInfo(
			"<player> <duration> <reason> - Chat bans the player for the specified time in minutes.",
			"<player> <remove> - Removes the chat ban of this player."
		);
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		Player player = World.getInstance().getPlayer(Util.convertName(params[0]));
		if (player == null || !player.isOnline()) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_MSG_ASK_PCINFO_LOGOFF());
			return;
		}

		if (params.length < 2) {
			sendInfo(admin);
			return;
		}

		if (params[1].equalsIgnoreCase("remove")) {
			if (!ChatBanService.isBanned(player)) {
				sendInfo(admin, "Player " + player.getName() + " can already chat.");
				return;
			}

			ChatBanService.unbanPlayer(player);
			sendInfo(admin, "Removed gag from player " + player.getName() + ".");
			return;
		}

		int time = 0;
		try {
			time = Integer.valueOf(params[1]);
		} catch (NumberFormatException e) {
			sendInfo(admin, "<duration> must be an int value (time in minutes).");
			return;
		}

		if (time < 1) {
			sendInfo(admin, "<duration> must be at least 1 minute.");
			return;
		}

		if (params.length < 3 || params[2].trim().length() <= 1) {
			sendInfo(admin, "<reason> must be specified.");
			return;
		}

		ChatBanService.banPlayer(player, time * 60000);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_INGAME_BLOCK_ENABLE_NO_CHAT(time));

		String reason = StringUtils.join(params, ' ', 2, params.length);
		sendInfo(player, StringUtils.appendIfMissing(StringUtils.capitalize(reason), ".", "!"));
		sendInfo(admin, "Player " + player.getName() + " is now gagged for " + time + " minutes.");
	}
}
