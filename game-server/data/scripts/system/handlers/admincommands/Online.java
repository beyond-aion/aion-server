package admincommands;

import java.util.Iterator;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author VladimirZ
 */
public class Online extends AdminCommand {

	public Online() {
		super("online");
	}

	@Override
	public void execute(Player admin, String... params) {

		int playerCount = DAOManager.getDAO(PlayerDAO.class).getOnlinePlayerCount();

		int elyosCount = 0;
		int asmoCount = 0;
		Iterator<Player> iter = World.getInstance().getPlayersIterator();

		while (iter.hasNext()) {
			Player player = iter.next();
			if(player.getRace() == Race.ELYOS){
				elyosCount++;
			}else if(player.getRace() == Race.ASMODIANS){
				asmoCount++;
			}
		}
		
		if (playerCount == 1) {
			PacketSendUtility.sendMessage(admin, "There is " + (playerCount) + " player online !");
		}
		else {
			PacketSendUtility.sendMessage(admin, "There are " + (playerCount) + " players online !");
			PacketSendUtility.sendMessage(admin, "There are " + (elyosCount) + " elyos players online !");
			PacketSendUtility.sendMessage(admin, "There are " + (asmoCount) + " asmo players online !");
		}
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "Syntax: //online");
	}
}
