package playercommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Wakizashi
 */
public class cmd_noexp extends PlayerCommand {

	public cmd_noexp() {
		super("noexp");
	}

	@Override
	public void execute(Player player, String... params) {
		if (player.getCommonData().getNoExp()) {
			player.getCommonData().setNoExp(false);
			PacketSendUtility.sendMessage(player, "Experience rewards are reactivated !");
		}
		else {
			player.getCommonData().setNoExp(true);
			PacketSendUtility.sendMessage(player, "Experience rewards are desactivated !");
		}
	}

	@Override
	public void onFail(Player player, String message) {
		// TODO Auto-generated method stub
	}
}
