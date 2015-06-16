package admincommands;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.PunishmentService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author nrg
 */
public class UnBanChar extends AdminCommand {

	public UnBanChar() {
		super("unbanchar");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params == null || params.length < 1) {
			PacketSendUtility.sendMessage(admin, "Syntax: //unbanchar <player>");
			return;
		}

		// Banned player must be offline
		String name = Util.convertName(params[0]);
		int playerId = DAOManager.getDAO(PlayerDAO.class).getPlayerIdByName(name);
		if (playerId == 0) {
			PacketSendUtility.sendMessage(admin, "Player " + name + " was not found!");
			PacketSendUtility.sendMessage(admin, "Syntax: //unbanchar <player>");
			return;
		}

		PacketSendUtility.sendMessage(admin, "Character " + name + " is not longer banned!");
		
    PunishmentService.unbanChar(playerId);
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "Syntax: //unban <player> [account|ip|full]");
	}
}
