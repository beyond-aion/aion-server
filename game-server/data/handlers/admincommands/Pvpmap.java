package admincommands;

import java.awt.Color;

import com.aionemu.gameserver.custom.pvpmap.PvpMapService;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Yeats
 */
public class Pvpmap extends AdminCommand {

	public Pvpmap() {
		super("pvpmap", "(De-)Activates the PvP-Map.");
		setSyntaxInfo("<activate | deactivate> (de-)activate the PvP-Map.\nDeactivating the PvP-Map will remove all players from the current map.");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			sendInfo(admin);
		} else {
			if (params[0].equalsIgnoreCase("activate")) {
				if (PvpMapService.getInstance().activate(admin)) {
					sendInfo(admin, "You have " + ChatUtil.color("activated", Color.GREEN) + " the PvP-Map.");
				} else {
					sendInfo(admin, "You cannot activate the PvP-Map, because it is already active.");
				}
			} else if (params[0].equalsIgnoreCase("deactivate")) {
				if (PvpMapService.getInstance().deactivate(admin)) {
					sendInfo(admin, "You have " + ChatUtil.color("deactivated", Color.RED) + " the PvP-Map.");
				} else {
					sendInfo(admin, "You cannot deactivate the PvP-Map, because it is already deactive.");
				}
			} else {
				sendInfo(admin);
			}
		}
	}

}
