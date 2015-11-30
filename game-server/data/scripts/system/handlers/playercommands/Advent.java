package playercommands;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.AdventDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.reward.AdventService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Nathan
 */
public class Advent extends PlayerCommand {

	public Advent() {
		super("advent", "Enables adventcalendar for this character");
	}

	@Override
	public void execute(Player player, String... params) {
		if (DAOManager.getDAO(AdventDAO.class).containAllready(player)) {
			PacketSendUtility.sendMessage(player, "You have already activated your calendar on another character!");
			return;
		} else {
			DAOManager.getDAO(AdventDAO.class).newAdvent(player);
			PacketSendUtility.sendMessage(player, "Your calendar was activated for this character.");
			AdventService.getInstance().onLogin(player);
		}
	}
}
