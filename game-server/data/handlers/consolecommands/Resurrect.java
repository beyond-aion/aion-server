package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RESURRECT;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Resurrect extends ConsoleCommand {

	public Resurrect() {
		super("resurrect", "Resurrects a player.");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (!((admin.isDead() ? admin : admin.getTarget()) instanceof Player player)) {
			sendInfo(admin, "Please select a player.");
			return;
		}

		if (!player.isDead()) {
			sendInfo(admin, player.equals(admin) ? "You're already alive." : player.getName() + " is already alive.");
			return;
		}

		player.setPlayerResActivate(true);
		PacketSendUtility.sendPacket(player, new SM_RESURRECT(admin));
	}
}
