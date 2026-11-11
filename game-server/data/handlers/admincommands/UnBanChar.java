package admincommands;

import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.PunishmentService;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author nrg
 */
public class UnBanChar extends AdminCommand {

	public UnBanChar() {
		super("unbanchar", "Removes a character ban.", """
			<player> - Unbans the character.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params == null || params.length < 1) {
			sendInfo(admin);
			return;
		}

		// Banned player must be offline
		String name = Util.convertName(params[0]);
		int playerId = PlayerDAO.getPlayerIdByName(name);
		if (playerId == 0) {
			sendInfo(admin, "Player " + name + " was not found!");
			return;
		}

		sendInfo(admin, "Character " + name + " is no longer banned.");

		PunishmentService.unbanChar(playerId);
	}
}
