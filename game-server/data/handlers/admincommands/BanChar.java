package admincommands;

import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.PunishmentService;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author nrg
 */
public class BanChar extends AdminCommand {

	public BanChar() {
		super("banchar", "Bans a character.", """
			<player> <days> <reason> - Bans the character for the given days, 0 means permanent.
			Note: The current day counts as a whole day, even if only a few hours are left.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params == null || params.length < 3) {
			sendInfo(admin);
			return;
		}

		int playerId = 0;
		String playerName = Util.convertName(params[0]);

		// First, try to find player in the World
		Player player = World.getInstance().getPlayer(playerName);
		if (player != null)
			playerId = player.getObjectId();

		// Second, try to get player Id from offline player from database
		if (playerId == 0)
			playerId = PlayerDAO.getPlayerIdByName(playerName);

		// Third, fail
		if (playerId == 0) {
			sendInfo(admin, "Player " + playerName + " was not found!");
			return;
		}

		int dayCount = Integer.parseInt(params[1]);

		if (dayCount < 0) {
			sendInfo(admin, "Days must be positive, or 0 for a permanent ban.");
			return;
		}

		String reason = Util.convertName(params[2]);
		for (int itr = 3; itr < params.length; itr++)
			reason += " " + params[itr];

		sendInfo(admin,
			"Character " + playerName + " is now banned " + (dayCount == 0 ? "permanently." : "for the next " + dayCount + " days."));

		PunishmentService.banChar(playerId, dayCount, reason);
	}
}
