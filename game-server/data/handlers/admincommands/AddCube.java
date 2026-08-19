package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.CubeExpandService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Kamui
 */
public class AddCube extends AdminCommand {

	public AddCube() {
		super("addcube");
	}

	@Override
	public void execute(Player admin, String... params) {

		if (params.length != 1) {
			PacketSendUtility.sendMessage(admin, "Syntax: //addcube <player name>");
			return;
		}

		Player receiver = null;

		receiver = World.getInstance().getPlayer(Util.convertName(params[0]));

		if (receiver == null) {
			PacketSendUtility.sendMessage(admin, "The player " + Util.convertName(params[0]) + " is not online.");
			return;
		}

		if (CubeExpandService.canExpand(receiver)) {
			CubeExpandService.npcExpand(receiver);
			PacketSendUtility.sendMessage(admin, "9 cube slots successfully added to player " + receiver.getName() + "!");
			PacketSendUtility.sendMessage(receiver, "Admin " + admin.getName() + " gave you a cube expansion!");
		} else {
			PacketSendUtility.sendMessage(admin, "Cube expansion cannot be added to " + receiver.getName()
				+ "!\nReason: player cube already fully expanded.");
			return;
		}
	}

	@Override
	public void info(Player admin, String message) {
		PacketSendUtility.sendMessage(admin, "Syntax: //addcube <player name>");
	}
}
