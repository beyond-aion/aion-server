package admincommands;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.PunishmentService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author nrg
 */
public class BanChar extends AdminCommand {

	public BanChar() {
		super("banchar");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params == null || params.length < 3) {
			sendInfo(admin, true);
			return;
		}
		
		int playerId = 0;
		String playerName = Util.convertName(params[0]);

		// First, try to find player in the World
		Player player = World.getInstance().findPlayer(playerName);
		if (player != null)
			playerId = player.getObjectId();

		// Second, try to get player Id from offline player from database
		if (playerId == 0)
			playerId = DAOManager.getDAO(PlayerDAO.class).getPlayerIdByName(playerName);

		// Third, fail
		if (playerId == 0) {
			PacketSendUtility.sendMessage(admin, "Player " + playerName + " was not found!");
			sendInfo(admin, true);
			return;
		}

		int dayCount = -1;
		try {
			dayCount = Integer.parseInt(params[1]);
		}
		catch (NumberFormatException e) {
			PacketSendUtility.sendMessage(admin, "Second parameter is not an int");
			sendInfo(admin, true);
			return;
		}
		
		if(dayCount < 0) {
			PacketSendUtility.sendMessage(admin, "Second parameter has to be a positive daycount or 0 for infinity");
			sendInfo(admin, true);;
			return;
		}

		String reason = Util.convertName(params[2]);
		for(int itr = 3; itr < params.length; itr++)
			reason += " "+params[itr];

		PacketSendUtility.sendMessage(admin, "Char " + playerName + " is now banned for the next "+dayCount+" days!");
		
		PunishmentService.banChar(playerId, dayCount, reason);
	}

	@Override
	public void onFail(Player player, String message) {
		sendInfo(player, false);
	}
	
	private void sendInfo(Player player, boolean withNote) {
		PacketSendUtility.sendMessage(player, "Syntax: //banChar <playername> <days>/0 (for permanent) <reason>");
		if(withNote)
		  PacketSendUtility.sendMessage(player, "Note: the current day is defined as a whole day even if it has just a few hours left!");
	}
}
