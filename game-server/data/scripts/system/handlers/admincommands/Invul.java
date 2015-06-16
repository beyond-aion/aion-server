package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Andy
 * @author Divinity - update
 */
public class Invul extends AdminCommand {

	public Invul() {
		super("invul");
	}

	@Override
	public void execute(Player player, String... params) {
		if (player.isInvul()) {
			player.setInvul(false);
			PacketSendUtility.sendMessage(player, "You are now mortal.");
		}
		else {
			player.setInvul(true);
			PacketSendUtility.sendMessage(player, "You are now immortal.");
		}
	}

	@Override
	public void onFail(Player player, String message) {
		// TODO Auto-generated method stub
	}
}
