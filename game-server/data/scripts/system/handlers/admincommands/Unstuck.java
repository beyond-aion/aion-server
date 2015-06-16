package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Nemiroff Date: 11.01.2010
 */
public class Unstuck extends AdminCommand {

	public Unstuck() {
		super("unstuck");
	}

	@Override
	public void execute(Player player, String... params) {
		if (player.getLifeStats().isAlreadyDead()) {
			PacketSendUtility.sendMessage(player, "You dont have execute this command. You die");
			return;
		}
		if (player.isInPrison()) {
			PacketSendUtility.sendMessage(player, "You can't use the unstuck command when you are in Prison");
			return;
		}

		TeleportService2.moveToBindLocation(player, true);
	}

	@Override
	public void onFail(Player player, String message) {
		// TODO Auto-generated method stub
	}
}
