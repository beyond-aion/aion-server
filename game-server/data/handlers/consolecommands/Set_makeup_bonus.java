package consolecommands;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATS_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Set_makeup_bonus extends ConsoleCommand {

	public Set_makeup_bonus() {
		super("set_makeup_bonus");
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

		long value;

		try {
			value = Long.parseLong(params[0]);
		} catch (NumberFormatException e) {
			info(admin, null);
			return;
		}

		player.getCommonData().setCurrentReposeEnergy(value);
		PacketSendUtility.sendPacket(player, new SM_STATS_INFO(player));
		PacketSendUtility.sendMessage(admin, "Repose Energy set to: " + value);
	}

	@Override
	public void info(Player admin, String message) {
		PacketSendUtility.sendMessage(admin, "syntax ///set_makeup_bonus <value>");
	}
}
