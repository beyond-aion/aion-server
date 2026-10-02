package admincommands;

import java.time.Duration;

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
		super("gag", "Bans a player from all chats.", """
			<player> <duration> <reason> - Chat bans the player for the specified time in minutes.
			<player> remove - Removes the chat ban of this player.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 2) {
			sendInfo(admin);
			return;
		}
		String playerName = Util.convertName(params[0]);
		Player player = World.getInstance().getPlayer(playerName);
		if (player == null) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(playerName));
			return;
		}
		if (params[1].equalsIgnoreCase("remove")) {
			if (ChatBanService.isBanned(player)) {
				ChatBanService.unbanPlayer(player);
				sendInfo(admin, "Unbanned " + name(player) + " from all chats.");
			} else {
				sendInfo(admin, name(player) + " can already chat.");
			}
		} else {
			int durationMinutes = Integer.parseInt(params[1]);
			if (durationMinutes < 1) {
				sendInfo(admin, "Duration must be at least 1 minute.");
				return;
			}
			String reason = join(params, 2);
			if (reason.isEmpty()) {
				sendInfo(admin, "Reason must be specified.");
				return;
			}
			ChatBanService.banPlayer(player, Duration.ofMinutes(durationMinutes).toMillis());
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_INGAME_BLOCK_ENABLE_NO_CHAT(durationMinutes));
			sendInfo(player, reason);
			sendInfo(admin, name(player) + " is now gagged for " + durationMinutes + " minute(s).");
		}
	}
}
