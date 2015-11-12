package consolecommands;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RESURRECT;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Resurrect extends ConsoleCommand {

	public Resurrect() {
		super("resurrect");
	}

	@Override
	public void execute(Player admin, String... params) {

		if (params.length < 1) {
			info(admin, null);
			return;
		}

		final VisibleObject target = admin.getTarget();
		if (target == null) {
			PacketSendUtility.sendMessage(admin, "No target selected.");
			return;
		}

		if (!(target instanceof Player)) {
			PacketSendUtility.sendMessage(admin, "This command can only be used on a player!");
			return;
		}

		final Player player = (Player) target;

		if (!player.getLifeStats().isAlreadyDead()) {
			PacketSendUtility.sendMessage(admin, "That player is already alive.");
			return;
		}

		player.setPlayerResActivate(true);
		PacketSendUtility.sendPacket(player, new SM_RESURRECT(admin));
	}
}
