package playercommands;

import com.aionemu.gameserver.custom.pvpmap.PvpMapService;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Yeats
 */
public class Pvp extends PlayerCommand {

	public Pvp() {
		super("pvp", "Join the custom PvP-Map where you can fight against the opposing faction.");

		setSyntaxInfo("<join | leave | info> - Join the PvP-Map by typing .pvp join.\nYou can leave the PvP-Map by typing .pvp leave.\nType .pvp info to see how many players are on the PvP-Map.");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0) {
			sendInfo(player);
		} else if (params.length >= 1) {
			if (params[0].equalsIgnoreCase("join")) {
				PvpMapService.getInstance().joinMap(player);
			} else if (params[0].equalsIgnoreCase("leave")) {
				PvpMapService.getInstance().leaveMap(player);
			} else if (params[0].equalsIgnoreCase("info")) {
				int size = PvpMapService.getInstance().getParticipantsSize();
				sendInfo(player, "There " + (size == 1 ? "is" : "are") + " currently " + (size == 0 ? "no" : size) + " player" + (size != 1 ? "s" : "") +" on the map.");
			} else {
				sendInfo(player);
			}
		}
	}
}
