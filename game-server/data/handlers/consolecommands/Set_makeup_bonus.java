package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATS_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Set_makeup_bonus extends ConsoleCommand {

	public Set_makeup_bonus() {
		super("set_makeup_bonus", "Sets repose energy.", """
			<value> - Sets the repose energy of your target (or yourself).
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			sendInfo(admin);
			return;
		}

		final Player player = admin.getTarget() instanceof Player target ? target : admin;

		long value = Long.parseLong(params[0]);

		player.getCommonData().setCurrentReposeEnergy(value);
		PacketSendUtility.sendPacket(player, new SM_STATS_INFO(player));
		sendInfo(admin, "Repose Energy set to: " + value);
	}
}
