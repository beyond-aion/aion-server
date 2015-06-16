package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Cura
 */
public class Cooldown extends AdminCommand {

	public Cooldown() {
		super("cooldown");
	}

	@Override
	public void execute(Player player, String... params) {
		if (player.isCoolDownZero()) {
			PacketSendUtility.sendMessage(player, "Cooldown time of all skills has been recovered.");
			player.setCoolDownZero(false);
		}
		else {
			PacketSendUtility.sendMessage(player, "Cooldown time of all skills is set to 0.");
			player.setCoolDownZero(true);
		}
	}

	@Override
	public void onFail(Player player, String message) {
		// TODO Auto-generated method stub
	}
}
